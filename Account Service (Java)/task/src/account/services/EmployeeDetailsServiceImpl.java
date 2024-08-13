package account.services;

import account.database.entities.EmployeeEntity;
import account.database.entities.PermissionEntity;
import account.database.repositories.EmployeeRepository;
import account.utilities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**This class is used by the framework for retrieving user details for the authentication process.
 * */
@Service
public class EmployeeDetailsServiceImpl implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeDetailsServiceImpl.class);

    private final EmployeeRepository employeeRepository;

    public EmployeeDetailsServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

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
                .authorities(entity.getPermissionEntities().stream()
                        .map(PermissionEntity::getRole)
                        .map(Role::getAuthority)
                        .map(SimpleGrantedAuthority::new)
                        .toList())
                .accountLocked(entity.getLockFlag())
                .build();
    }

}
