package account.utilities;

import account.database.repositories.BreachedPasswordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.stereotype.Component;

/**This class is used to check if the client-provided password is compromised or not
 * */
@Component
public class BreachedPasswordChecker implements CompromisedPasswordChecker {
    private static final Logger logger = LoggerFactory.getLogger(BreachedPasswordChecker.class);

    private final BreachedPasswordRepository breachedPasswordRepository;

    public BreachedPasswordChecker(BreachedPasswordRepository breachedPasswordRepository) {
        this.breachedPasswordRepository = breachedPasswordRepository;
    }

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
