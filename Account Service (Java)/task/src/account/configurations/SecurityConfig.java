package account.configurations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

import static account.utilities.Role.*;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(13);
    }

    /**
     * Configure the filter chain for all requests in Spring Security.
     * This chain is called by FilterChainProxy.java within the framework
     * */
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
                .userDetailsService(userDetailsService)
                .authorizeHttpRequests(auth -> auth  // manage access
                        .requestMatchers(HttpMethod.POST, "/api/auth/changepass").hasAnyAuthority(USER.getAuthority(), ACCOUNTANT.getAuthority(), ADMINISTRATOR.getAuthority())
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/empl/payment").hasAnyAuthority(USER.getAuthority(), ACCOUNTANT.getAuthority())
                        .requestMatchers(HttpMethod.GET, "/api/security/events/").hasAuthority(AUDITOR.getAuthority())
                        .requestMatchers(HttpMethod.POST, "/api/acct/payments").hasAuthority(ACCOUNTANT.getAuthority())
                        .requestMatchers(HttpMethod.PUT, "/api/acct/payments").hasAuthority(ACCOUNTANT.getAuthority())
                        .requestMatchers(HttpMethod.GET, "/api/admin/user/").hasAuthority(ADMINISTRATOR.getAuthority())
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/user/{email}").hasAuthority(ADMINISTRATOR.getAuthority())
                        .requestMatchers(HttpMethod.PUT, "/api/admin/user/role").hasAuthority(ADMINISTRATOR.getAuthority())
                        .requestMatchers(HttpMethod.PUT, "/api/admin/user/access").hasAuthority(ADMINISTRATOR.getAuthority())

                        .requestMatchers("/error/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll() // enable for testing purposes
                        .requestMatchers("/h2-console/**").permitAll() // enable h2 console to inspect db

                )
                .sessionManagement(sessions -> sessions
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no session
                );

        return http.build();
    }


    /**This class is used for commencing the authentication process
     * when an unauthenticated client tries to access a secured resource.
     * */
    private class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

        private static final Logger logger = LoggerFactory.getLogger(RestAuthenticationEntryPoint.class);

        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
            logger.error(String.format("unauthenticated client attempted to access resource %s", request.getRequestURI()));
            if (authException instanceof LockedException) {
                setResponse(request, response, HttpStatus.UNAUTHORIZED, "User account is locked");
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
            }
        }
    }

    /**This class is used for handling the response when an authenticated client
     * attempts to access an unauthorized resource.
     * */
    private class RestAccessDeniedHandler implements AccessDeniedHandler {

        private static final Logger logger = LoggerFactory.getLogger(RestAccessDeniedHandler.class);

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (Objects.nonNull(auth)) {
                logger.warn(String.format("authenticated user: %s attempted to access unauthorized resource: %s", auth.getName(), request.getRequestURI()));
            } else {
                logger.error(String.format("unauthenticated user attempted to access protected resource: %s", request.getRequestURI()));
            }
            setResponse(request, response, HttpStatus.FORBIDDEN, "Access Denied!");
        }

    }

    private void setResponse(HttpServletRequest request, HttpServletResponse response, HttpStatus httpStatus, String message) throws IOException {
        response.setStatus(httpStatus.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(buildResponseBody(httpStatus, request.getRequestURI(), message));
        response.getWriter().close();
    }

    private String buildResponseBody(HttpStatus httpStatus, String path, String message) {
        // build response object
        ResponseBody body = ResponseBody.builder()
                .timestamp(LocalDateTime.now())
                .status(httpStatus.value())
                .error(httpStatus.getReasonPhrase())
                .message(message)
                .path(path)
                .build();
        // build json mapper
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        // convert to json
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    @Builder
    @JsonPropertyOrder({"timestamp", "status", "error", "message", "path"})
    private static class ResponseBody {
        @JsonProperty("timestamp")
        private LocalDateTime timestamp;
        @JsonProperty("status")
        private int status;
        @JsonProperty("error")
        private String error;
        @JsonProperty("message")
        private String message;
        @JsonProperty("path")
        private String path;
    }

}
