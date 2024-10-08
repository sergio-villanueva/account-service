package account.controllers;

import account.exceptions.*;
import account.validations.ValidationMessages;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.password.CompromisedPasswordException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ExceptionHandlerController extends ResponseEntityExceptionHandler {

    private final static Logger logger = LoggerFactory.getLogger(ExceptionHandlerController.class);

    private final HttpServletRequest httpServletRequest;

    @Autowired
    public ExceptionHandlerController(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    @ExceptionHandler(ModifyAdminAccessException.class)
    public ResponseEntity<Object> handleModifyAdminAccessException(ModifyAdminAccessException e) {
        return new ResponseEntity<>(buildBodyWithMessage(HttpStatus.BAD_REQUEST, e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RemoveRoleNotFoundException.class)
    public ResponseEntity<Object> handleRemoveRoleNotFoundException(RemoveRoleNotFoundException e) {
        return new ResponseEntity<>(buildBodyWithMessage(HttpStatus.BAD_REQUEST, e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RemoveLastRoleException.class)
    public ResponseEntity<Object> handleRemoveLastRoleException(RemoveLastRoleException e) {
        return new ResponseEntity<>(buildBodyWithMessage(HttpStatus.BAD_REQUEST, e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RemoveAdminException.class)
    public ResponseEntity<Object> handleRemoveAdminException(RemoveAdminException e) {
        return new ResponseEntity<>(buildBodyWithMessage(HttpStatus.BAD_REQUEST, e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<Object> handleInvalidRoleException(InvalidRoleException e) {
        return new ResponseEntity<>(buildBodyWithMessage(HttpStatus.NOT_FOUND, e.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(GrantAdminRoleException.class)
    public ResponseEntity<Object> handleGrantAdminRoleException(GrantAdminRoleException e) {
        return new ResponseEntity<>(buildBodyWithMessage(HttpStatus.BAD_REQUEST, e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(GrantBusinessRoleException.class)
    public ResponseEntity<Object> handleGrantBusinessRoleException(GrantBusinessRoleException e) {
        return new ResponseEntity<>(buildBodyWithMessage(HttpStatus.BAD_REQUEST, e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DeleteAdminException.class)
    public ResponseEntity<Object> handleDeleteAdminException(DeleteAdminException e) {
        return new ResponseEntity<>(buildBodyWithMessage(HttpStatus.BAD_REQUEST, e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<Object> handleEmployeeNotFoundException(EmployeeNotFoundException e) {
        return new ResponseEntity<>(buildBodyWithMessage(HttpStatus.NOT_FOUND, e.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmailDoesNotExistException.class)
    public ResponseEntity<Object> handleEmailDoesNotExistException(EmailDoesNotExistException e) {
        logger.error("employee email does not exist: " + e.getMessage());
        return new ResponseEntity<>(buildBodyWithMessage(HttpStatus.BAD_REQUEST, e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PeriodAlreadyExistsException.class)
    public ResponseEntity<Object> handlePeriodAlreadyExistsException(PeriodAlreadyExistsException e) {
        logger.error("employee period already exists: " + e.getMessage());
        return new ResponseEntity<>(buildBodyWithMessage(HttpStatus.BAD_REQUEST, e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PeriodDoesNotExistException.class)
    public ResponseEntity<Object> handlePeriodDoesNotExistException(PeriodDoesNotExistException e) {
        logger.error("employee period does not exist: " + e.getMessage());
        return new ResponseEntity<>(buildBodyWithMessage(HttpStatus.BAD_REQUEST, e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CompromisedPasswordException.class)
    public ResponseEntity<Object> handleCompromisedPasswordException(CompromisedPasswordException e) {
        // requirement today is to response w/ 400 bad request
        logger.error("compromised password exception: " + e.getMessage());
        return new ResponseEntity<>(buildBodyWithMessage(HttpStatus.BAD_REQUEST, ValidationMessages.BREACHED_PASSWORD),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IdenticalPasswordsException.class)
    public ResponseEntity<Object> handleIdenticalPasswordsException(IdenticalPasswordsException e) {
        logger.error("identical passwords exception: " + e.getMessage());
        return new ResponseEntity<>(buildBodyWithMessage(HttpStatus.BAD_REQUEST, e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Object> handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
        logger.error("email already exists exception: " + e.getMessage());
        return new ResponseEntity<>(buildBodyWithMessage(HttpStatus.BAD_REQUEST, e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> handleUsernameNotFoundException(UsernameNotFoundException e) {
        logger.error("username not found exception: " + e.getMessage());
        return new ResponseEntity<>(buildExceptionalBody(HttpStatus.NOT_FOUND),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException e) {
        logger.error("bad credentials exception: " + e.getMessage());
        return new ResponseEntity<>(buildBodyWithMessage(HttpStatus.UNAUTHORIZED, ""),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Object> handleNoSuchElementException(NoSuchElementException e) {
        logger.error("no such element exception: " + e.getMessage());
        return new ResponseEntity<>(buildExceptionalBody(HttpStatus.NOT_FOUND),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException e) {
        logger.error("invalid arguments: " + e.getMessage());
        return new ResponseEntity<>(buildBodyWithMessage(HttpStatus.BAD_REQUEST, e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                               HttpStatusCode status, WebRequest webRequest) {
        // all bean validation failures will have the same response structure
        String message = ex.getBindingResult().getFieldErrors().stream().findFirst().map(FieldError::getDefaultMessage).orElse(null);
        // extracted the detailed message defined from bean annotation; never extracts null
        logger.error("an argument is not valid: " + ex.getMessage());
        return new ResponseEntity<>(buildBodyWithMessage(HttpStatus.BAD_REQUEST, message),
                HttpStatus.BAD_REQUEST);
    }

    private Map<String, Object> buildExceptionalBody(HttpStatus status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        return body;
    }

    private Map<String, Object> buildBodyWithMessage(HttpStatus status, String message) {
        Map<String, Object> body = buildExceptionalBody(status);
        body.put("message", message);
        body.put("path", httpServletRequest.getRequestURI());
        return body;
    }

}
