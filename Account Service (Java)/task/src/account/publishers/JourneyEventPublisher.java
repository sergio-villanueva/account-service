package account.publishers;

import account.models.events.JourneyEvent;
import account.utilities.EventTypes;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class JourneyEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    private final HttpServletRequest httpServletRequest;

    private final static Logger logger = LoggerFactory.getLogger(JourneyEventPublisher.class);


    @Autowired
    public JourneyEventPublisher(ApplicationEventPublisher applicationEventPublisher, HttpServletRequest httpServletRequest) {
        // fetches http request for both web request thread or listener thread (thread safety)
        this.applicationEventPublisher = applicationEventPublisher;
        this.httpServletRequest = httpServletRequest;
    }

    public void publishCreateEmployeeEvent(String email) {
        logger.info(String.format("publishing create employee event for employee email: %s", email));
        applicationEventPublisher.publishEvent(buildJourneyEvent(EventTypes.CREATE_USER, "Anonymous", email));
    }

    public void publishChangePasswordEvent(String email) {
        logger.info(String.format("publishing change password event for employee email: %s", email));
        applicationEventPublisher.publishEvent(buildJourneyEvent(EventTypes.CHANGE_PASSWORD, email, email));
    }

    public void publishGrantRoleEvent(String adminEmail, String employeeEmail, String requestedRole) {
        logger.info(String.format("publishing grant role event for employee email %s and requested role %s", employeeEmail, requestedRole));
        logger.info(String.format("grant role request for %s was triggered by admin %s",employeeEmail, adminEmail));
        String object = String.format("Grant role %s to %s", requestedRole, employeeEmail.toLowerCase());
        applicationEventPublisher.publishEvent(buildJourneyEvent(EventTypes.GRANT_ROLE, adminEmail, object));
    }

    public void publishRemoveRoleEvent(String adminEmail, String employeeEmail, String requestedRole) {
        logger.info(String.format("publishing remove role event for employee email %s and requested role %s", employeeEmail, requestedRole));
        logger.info(String.format("remove role request for %s was triggered by admin %s",employeeEmail, adminEmail));
        String object = String.format("Remove role %s from %s", requestedRole, employeeEmail.toLowerCase());
        applicationEventPublisher.publishEvent(buildJourneyEvent(EventTypes.REMOVE_ROLE, adminEmail, object));
    }

    public void publishDeleteEmployeeEvent(String adminEmail, String employeeEmail) {
        logger.info(String.format("publishing delete employee event for employee %s", employeeEmail));
        logger.info(String.format("delete employee event for employee email %s was triggered by admin %s", employeeEmail, adminEmail));
        applicationEventPublisher.publishEvent(buildJourneyEvent(EventTypes.DELETE_USER, adminEmail, employeeEmail));
    }

    public void publishLockEmployeeAccessEvent(String email) {
        final String object = String.format("Lock user %s", email);
        logger.info(String.format("publishing lock employee event for employee %s", email));
        applicationEventPublisher.publishEvent(buildJourneyEvent(EventTypes.LOCK_USER, email, object));
    }

    public void publishLockEmployeeAccessAsyncEvent(String email, String path) {
        final String object = String.format("Lock user %s", email);
        logger.info(String.format("publishing lock employee event for employee %s", email));
        applicationEventPublisher.publishEvent(buildJourneyEvent(EventTypes.LOCK_USER, email, object, path));
    }

    public void publishUnlockEmployeeAccessEvent(String email) {
        final String object = String.format("Unlock user %s", email);
        logger.info(String.format("publishing unlock employee event for employee %s", email));
        applicationEventPublisher.publishEvent(buildJourneyEvent(EventTypes.UNLOCK_USER, email, object));
    }

    public void publishUnlockEmployeeAccessAsyncEvent(String email, String path) {
        final String object = String.format("Unlock user %s", email);
        logger.info(String.format("publishing unlock employee event for employee %s", email));
        applicationEventPublisher.publishEvent(buildJourneyEvent(EventTypes.UNLOCK_USER, email, object, path));
    }

    private JourneyEvent buildJourneyEvent(String eventType, String subject, String object, String path) {
        logger.info(String.format("request path: %s", path));
        return JourneyEvent.builder()
                .date(LocalDateTime.now())
                .eventType(eventType)
                .subject(subject)
                .object(object)
                .path(path)
                .build();
    }

    private JourneyEvent buildJourneyEvent(String eventType, String subject, String object) {
        final String path = httpServletRequest.getRequestURI();
        return buildJourneyEvent(eventType, subject, object, path);
    }
}
