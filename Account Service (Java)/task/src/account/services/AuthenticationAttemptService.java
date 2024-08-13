package account.services;

import account.exceptions.EmployeeNotFoundException;
import account.exceptions.ModifyAdminAccessException;
import account.models.requests.ModifyAccessRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AuthenticationAttemptService {

    private final Cache cache;

    private final EmployeeService employeeService;

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationAttemptService.class);

    @Autowired
    public AuthenticationAttemptService(CacheManager cacheManager, EmployeeService employeeService) {
        this.cache = cacheManager.getCache("attemptsCache");
        this.employeeService = employeeService;
    }

    /** This method is used to increment the failed login counter for a given employee
     * @param email the email of the employee
     * */
    public void loginFailed(String email, String path) {
        // fetch the failed login counter
        AtomicInteger failedAttempts = cache.get(email, AtomicInteger.class);
        if (Objects.nonNull(failedAttempts)) {
            // handle failed attempt
            handleFailedAttempt(email, failedAttempts, path);
        } else {
            // insert employee into cache
            logger.info(String.format("starting new failed attempt counter for %s", email));
            cache.putIfAbsent(email, new AtomicInteger(1));
        }
    }

    private void handleFailedAttempt(String email, AtomicInteger failedAttemptsCounter, String path) {
        // increment the counter
        cache.putIfAbsent(email, failedAttemptsCounter.incrementAndGet());
        // lock the employee if brute force attempt is detected
        if (failedAttemptsCounter.get() >= 5) {
            try {
                employeeService.modifyEmployeeAccessAsync(ModifyAccessRequest.builder()
                        .email(email)
                        .operation("LOCK")
                        .build(), path);
                // log for identifying normal brute force event
                logger.info(String.format("%s is now locked", email));
            } catch (ModifyAdminAccessException e) {
                // log for identifying brute force event on an administrator
                logger.warn(String.format("administrator %s cannot be locked and has %d failed authentication attempts",
                        email,
                        failedAttemptsCounter.get()));
            } catch (EmployeeNotFoundException e) {
                // log for identifying an event with non-existent employee
                // this case should never happen
                logger.warn(String.format("employee %s does not exist in the database and could not be locked", email));
            }
        } else {
            // log for identifying only a failed authentication event
            logger.info(String.format("%s has %d failed attempts so far", email, failedAttemptsCounter.get()));
        }
    }

    /** This method is used to evict an employee if able to successfully login
     * @param email the email of the employee
     * */
    public void loginPassed(String email) {
        cache.evictIfPresent(email);
    }

}
