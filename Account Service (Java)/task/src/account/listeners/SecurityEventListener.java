package account.listeners;

import account.database.entities.CommonEventEntity;
import account.database.entities.EmployeeEntity;
import account.database.repositories.CommonEventRepository;
import account.database.repositories.EmployeeRepository;
import account.services.AuthenticationAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.Executor;

import static account.utilities.EventTypes.*;

@Component
public class SecurityEventListener {

    private final static Logger logger = LoggerFactory.getLogger(SecurityEventListener.class);

    private final HttpServletRequest httpServletRequest;

    private final CommonEventRepository commonEventRepository;

    private final EmployeeRepository employeeRepository;

    private final AuthenticationAttemptService authenticationAttemptService;

    private final Executor listenerExecutor;

    @Autowired
    public SecurityEventListener(HttpServletRequest httpServletRequest, CommonEventRepository commonEventRepository, EmployeeRepository employeeRepository, AuthenticationAttemptService authenticationAttemptService, Executor listenerExecutor) {
        this.httpServletRequest = httpServletRequest;
        this.commonEventRepository = commonEventRepository;
        this.employeeRepository = employeeRepository;
        this.authenticationAttemptService = authenticationAttemptService;
        this.listenerExecutor = listenerExecutor;
    }

    //Note: DO NOT PASS the Authentication object outside the request thread as this presents a security vulnerability

    /** This listener is used to pick up failed password events
     * @param securityEvent the failed password event
     * */
    @EventListener
    public void onAuthenticationFailureBadCredentialsEvent(AuthenticationFailureBadCredentialsEvent securityEvent) {
        // fetch required data for event processing
        final String path = httpServletRequest.getRequestURI();
        final String email = securityEvent.getAuthentication().getName();
        listenerExecutor.execute(() -> handleFailedAuthenticationAttempt(email, path));
    }

    /** This method is used to handle failed authentication attempts
     * @param email the email attempting authentication
     * @param path the request path where authentication failed
     * */
    private void handleFailedAuthenticationAttempt(String email, String path) {
        // STEP 1: Create event entity for failed login attempt
        final CommonEventEntity commonEventEntity = buildCommonEventEntity(LOGIN_FAILED, email, path);
        // STEP 2: Save failed login event in database
        commonEventRepository.save(commonEventEntity);
        logger.info(String.format("successfully saved failed login attempt event from %s in database", email));
        // STEP 3: Record failed login attempt in the cache
        authenticationAttemptService.loginFailed(email, path);
    }

    /** This listener is used to pick up successful authentication events
     * @param securityEvent the successful authentication events
     * */
    @EventListener
    public void onAuthenticationSuccessEvent(AuthenticationSuccessEvent securityEvent) {
        final String email = securityEvent.getAuthentication().getName();
        listenerExecutor.execute(() -> handlePassedAuthenticationAttempt(email));
    }

    /** This method is used to handle successful authentication events
     * @param email the email attempting authentication
     * */
    private void handlePassedAuthenticationAttempt(String email) {
        // reset the failed login attempt counter
        authenticationAttemptService.loginPassed(email);
        logger.info(String.format("successfully reset the failed login counter for %s", email));
    }

    /** This listener is used to pick up failed authorization events
     * @param securityEvent the failed authorization event
     * */
    @EventListener
    public void onAuthorizationDeniedEvent(AuthorizationDeniedEvent<?> securityEvent) {
        final String path = httpServletRequest.getRequestURI();
        final String email = securityEvent.getAuthentication().get().getName();
        listenerExecutor.execute(() -> handleFailedAuthorizationAttempt(email, path));

    }

    /** This method is used to handle failed authorization events
     * @param email the email attempting authorization
     * @param path the request path where authentication failed
     * */
    private void handleFailedAuthorizationAttempt(String email, String path) {
        // check if email is registered
        if (!employeeRepository.existsByEmailIgnoreCase(email)) {
            // do not pollute events table with failed authz events from non-registered emails
            final String pollutionMessage = String
                    .format("failed authorization event will not be saved as email %s is not registered in database", email);
            logger.warn(pollutionMessage);
            return;
        }
        // create event entity for authorization denied attempt and save in db
        commonEventRepository.save(buildCommonEventEntity(ACCESS_DENIED, email, path));
        logger.info(String.format("successfully saved failed authorization attempt event from %s in database", email));
    }

    private boolean isAccountLocked(String email) {
        Optional<EmployeeEntity> optional = employeeRepository.findByEmailIgnoreCase(email);
        return optional.isPresent() && optional.get().getLockFlag();
    }

    private CommonEventEntity buildCommonEventEntity(String eventType, String employeeEmail, String path) {
        return CommonEventEntity.builder()
                .created(LocalDateTime.now())
                .eventType(eventType)
                .subject(employeeEmail) // employee email
                .object(path) // path
                .path(path) // path
                .build();
    }

}
