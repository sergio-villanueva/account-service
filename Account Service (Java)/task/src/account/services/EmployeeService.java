package account.services;

import account.database.entities.EmployeeEntity;
import account.database.repositories.BreachedPasswordRepository;
import account.database.repositories.EmployeeRepository;
import account.exceptions.EmailAlreadyExistsException;
import account.exceptions.IdenticalPasswordsException;
import account.models.requests.ChangePasswordRequest;
import account.models.requests.Registration;
import account.models.dto.UserDTO;
import account.utilities.Endpoints;
import account.validations.ValidationMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final BreachedPasswordRepository breachedPasswordRepository;

    private final CompromisedPasswordChecker compromisedPasswordChecker;

    private final PasswordEncoder passwordEncoder;

    private final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, BreachedPasswordRepository breachedPasswordRepository, CompromisedPasswordChecker compromisedPasswordChecker, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.breachedPasswordRepository = breachedPasswordRepository;
        this.compromisedPasswordChecker = compromisedPasswordChecker;
        this.passwordEncoder = passwordEncoder;
    }

    /** Saves the registration details into the database
     * @param registration the registration details
     * @return {@code UserDTO}
     * */
    public UserDTO createUser(Registration registration) {

        // STEP 1: Validate for breached password
        if (compromisedPasswordChecker.check(registration.getPassword()).isCompromised()) {
            logger.error("new password IS breached");
            throw new CompromisedPasswordException("new password IS breached");
        }
        logger.info("new password IS NOT breached");

        // STEP 2: Check if email is already registered; Email should be case-insensitive
        registration.setEmail(registration.getEmail().toLowerCase());
        if (employeeRepository.existsByEmailIgnoreCase(registration.getEmail())) {
            logger.info("email is already associated with a user");
            throw new EmailAlreadyExistsException("User exist!", Endpoints.SIGNUP);
        }

        // STEP 3: Build entity
        EmployeeEntity employeeEntity = EmployeeEntity.builder()
                .firstName(registration.getFirstName())
                .lastName(registration.getLastName())
                .email(registration.getEmail())
                .password(passwordEncoder.encode(registration.getPassword()))
                .created(LocalDateTime.now())
                .payrollEntities(new ArrayList<>())
                .build();

        // Step 4: Save user in database and return DTO
        return toUserDTO(employeeRepository.save(employeeEntity));
    }

    public UserDTO changePassword(UserDetails userDetails, ChangePasswordRequest request) {
        // STEP 1: Check if new password is compromised
        if (compromisedPasswordChecker.check(request.getNewPassword()).isCompromised()) {
            logger.error("new password IS breached");
            throw new CompromisedPasswordException("new password IS breached");
        }
        logger.info("new password IS NOT breached");

        // STEP 2: Fetch current password from db rather than authentication to prevent access to plaintext password
        Optional<EmployeeEntity> optionalUserEntity = employeeRepository.findByEmailIgnoreCase(userDetails.getUsername());

        // STEP 3: Ensure passwords are not identical
        optionalUserEntity.ifPresent((userEntity) -> {
            if (passwordEncoder.matches(request.getNewPassword(), userEntity.getPassword())) {
                logger.error("passwords are identical");
                throw new IdenticalPasswordsException(ValidationMessages.IDENTICAL_PASSWORDS);
            }
            logger.info("new password is different");
        });

        // STEP 4: Change password and save details
        optionalUserEntity.ifPresent((userEntity) -> {
            userEntity.setPassword(passwordEncoder.encode(request.getNewPassword()));
            employeeRepository.save(userEntity);
            logger.info("new password is saved");
        });

        // STEP 5: Return user data to form response
        return toUserDTO(optionalUserEntity.orElseThrow(() -> {
            // this case should never execute as user has already authenticated
            logger.error("could not find user data despite passing authentication");
            return new UsernameNotFoundException("could not find user data despite passing authentication");
        }));
    }

    private UserDTO toUserDTO(EmployeeEntity entity) {
        logger.info("user successfully saved in database");
        return UserDTO.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .build();
    }

}
