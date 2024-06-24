package account.configurations;

import account.database.entities.EmployeeEntity;
import account.database.repositories.BreachedPasswordRepository;
import account.database.repositories.EmployeeRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.util.Arrays;

@Configuration
public class SecurityConfig {

    private final EmployeeRepository employeeRepository;

    private final BreachedPasswordRepository breachedPasswordRepository;

    @Autowired
    public SecurityConfig(EmployeeRepository employeeRepository, BreachedPasswordRepository breachedPasswordRepository) {
        this.employeeRepository = employeeRepository;
        this.breachedPasswordRepository = breachedPasswordRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(13);
    }

    @Bean
    public CompromisedPasswordChecker compromisedPasswordChecker() {
        return new BreachedPasswordChecker();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable) // For Postman
                .headers(headers -> headers.frameOptions(Customizer.withDefaults()).disable()) // For the H2 console
                .exceptionHandling(handling -> {
                    handling.authenticationEntryPoint(new RestAuthenticationEntryPoint());
                    handling.accessDeniedHandler(new RestAccessDeniedHandler());
                }) // Handle auth errors
                .userDetailsService(new UserDetailsServiceImpl())
                .authorizeHttpRequests(auth -> auth  // manage access
                        .requestMatchers(HttpMethod.POST, "/api/auth/changepass").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/empl/payment").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/acct/payments").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/acct/payments").permitAll()

                        .requestMatchers("/error/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll() // enable for testing purposes
                        .requestMatchers("/h2-console/**").permitAll() // enable h2 console to inspect db

                )
                .sessionManagement(sessions -> sessions
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no session
                );

        return http.build();
    }

    /**This class is used by the framework for retrieving user details for the authentication process.
     * */
    private class UserDetailsServiceImpl implements UserDetailsService {

        private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

        /**
         * Locates the user based on the email. In the actual implementation, the search
         * may be case-sensitive, or case-insensitive depending on how the
         * implementation instance is configured. In this case, the <code>UserDetails</code>
         * object that comes back may have an email that is of a different case than what
         * was actually requested.
         *
         * @param email the email identifying the user whose data is required.
         * @return a fully populated user record (never <code>null</code>)
         * @throws UsernameNotFoundException if the user could not be found or the user has no
         *                                   GrantedAuthority
         */
        @Override
        public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
            logger.info("start search for user details for authentication");
            logger.info(String.format("requested email: %s", email));
            return employeeRepository.findByEmailIgnoreCase(email)
                    .map(this::toUserDetails)
                    .orElseThrow(() -> {
                        logger.error("The following username does not exist: " + email);
                        return new UsernameNotFoundException("The following email does not exist: " + email);
                    });
        }

        private UserDetails toUserDetails(EmployeeEntity entity) {
            logger.info("stored email: " + entity.getEmail());
            logger.info("stored password: " + entity.getPassword());
            return User.builder()
                    .username(entity.getEmail())
                    .password(entity.getPassword())
                    .roles("ADMINISTRATOR")
                    .build();
        }
    }

    private class BreachedPasswordChecker implements CompromisedPasswordChecker {

        private static final Logger logger = LoggerFactory.getLogger(BreachedPasswordChecker.class);

        @Override
        @NonNull
        public CompromisedPasswordDecision check(String password) {
            /*todo: what happens if a user's password is labeled as breached in the future?
                how will they change password if they can never login (authenticate)?
            */
            boolean compromisedFlag = breachedPasswordRepository.existsByPassword(password);
            if (compromisedFlag) {
                logger.error("password IS breached");
            } else {
                logger.info("password IS NOT breached");
            }

            return new CompromisedPasswordDecision(compromisedFlag);
        }
    }

    /**This class is used for commencing the authentication process
     * when an unauthenticated client tries to access a secured resource.
     * */
    private class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

        private static final Logger logger = LoggerFactory.getLogger(RestAuthenticationEntryPoint.class);

        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
            logger.error("unauthenticated client attempted to access secured resources");
            logger.error(Arrays.toString(authException.getStackTrace()));
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
        }
    }

    /**This class is used for handling the response when an authenticated client
     * attempts to access an unauthorized resource.
     * */
    private class RestAccessDeniedHandler implements AccessDeniedHandler {

        private static final Logger logger = LoggerFactory.getLogger(RestAccessDeniedHandler.class);

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
            logger.error("authenticated client attempted to access unauthorized resources");
            logger.error(Arrays.toString(accessDeniedException.getStackTrace()));
            response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage());
        }
    }


}
