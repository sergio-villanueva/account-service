package account.services;

import account.database.entities.EmployeeEntity;
import account.database.entities.PermissionEntity;
import account.database.repositories.EmployeeRepository;
import account.exceptions.*;
import account.models.dto.EmployeeDTO;
import account.models.requests.ChangePasswordRequest;
import account.models.requests.ModifyRoleRequest;
import account.models.requests.Registration;
import account.utilities.Role;
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
import java.util.*;
import java.util.stream.Collectors;

import static account.utilities.Role.*;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final CompromisedPasswordChecker compromisedPasswordChecker;

    private final PasswordEncoder passwordEncoder;

    private final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, CompromisedPasswordChecker compromisedPasswordChecker, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.compromisedPasswordChecker = compromisedPasswordChecker;
        this.passwordEncoder = passwordEncoder;
    }

    /** Saves the registration details into the database
     * @param registration the registration details
     * @return {@code EmployeeDTO}
     * */
    public EmployeeDTO createEmployee(Registration registration) {

        // STEP 1: Validate for breached password
        if (compromisedPasswordChecker.check(registration.getPassword()).isCompromised()) {
            throw new CompromisedPasswordException("new password IS breached");
        }

        // STEP 2: Check if email is already registered; Email should be case-insensitive
        String originalEmail = registration.getEmail();
        if (employeeRepository.existsByEmailIgnoreCase(registration.getEmail())) {
            logger.error(String.format("email: %s is already associated with an employee", registration.getEmail()));
            throw new EmailAlreadyExistsException("User exist!");
        }

        // STEP 3: Determine employee role
        boolean userIndicator = employeeRepository.existsById(1L);

        // STEP 4: Build entity
        PermissionEntity permissionEntity = PermissionEntity.builder()
                .role(userIndicator ? USER : ADMINISTRATOR)
                .build();

        EmployeeEntity employeeEntity = EmployeeEntity.builder()
                .firstName(registration.getFirstName())
                .lastName(registration.getLastName())
                .email(registration.getEmail().toLowerCase())
                .password(passwordEncoder.encode(registration.getPassword()))
                .created(LocalDateTime.now())
                .payrollEntities(new ArrayList<>())
                .permissionEntities(new HashSet<>(Set.of(permissionEntity)))
                .build();

        permissionEntity.setEmployeeEntity(employeeEntity);

        // Step 5: Save employee in database and return DTO
        EmployeeEntity savedEntity = employeeRepository.save(employeeEntity);
        savedEntity.setEmail(originalEmail);
        return toEmployeeDTO(savedEntity);
    }

    public EmployeeDTO changePassword(UserDetails userDetails, ChangePasswordRequest request) {
        // STEP 1: Check if new password is compromised
        logger.info("checking if new password is compromised");
        if (compromisedPasswordChecker.check(request.getNewPassword()).isCompromised()) {
            throw new CompromisedPasswordException("new password IS breached");
        }

        // STEP 2: Fetch current password from db rather than authentication to prevent access to plaintext password
        Optional<EmployeeEntity> optionalEmployeeEntity = employeeRepository.findByEmailIgnoreCase(userDetails.getUsername());

        // STEP 3: Ensure passwords are not identical
        optionalEmployeeEntity.ifPresent((employeeEntity) -> {
            logger.info(String.format("found employee with email: %s", userDetails.getUsername()));
            if (passwordEncoder.matches(request.getNewPassword(), employeeEntity.getPassword())) {
                logger.error("passwords are identical");
                throw new IdenticalPasswordsException(ValidationMessages.IDENTICAL_PASSWORDS);
            }
            logger.info("new password is different");
        });

        // STEP 4: Change password and save details
        optionalEmployeeEntity.ifPresent((employeeEntity) -> {
            employeeEntity.setPassword(passwordEncoder.encode(request.getNewPassword()));
            employeeRepository.save(employeeEntity);
            logger.info("new password is saved");
        });

        // STEP 5: Return user data to form response
        return toEmployeeDTO(optionalEmployeeEntity.orElseThrow(() -> {
            // this case should never execute as user has already authenticated
            logger.error("could not find user data despite passing authentication");
            return new UsernameNotFoundException("could not find user data despite passing authentication");
        }));
    }

    private EmployeeDTO toEmployeeDTO(EmployeeEntity entity) {
        logger.info("employee successfully saved in database");
        return EmployeeDTO.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .roles(entity.getPermissionEntities().stream()
                        .map(PermissionEntity::getRole)
                        .collect(Collectors.toSet()))
                .build();
    }

    public List<EmployeeDTO> retrieveEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::toEmployeeDTO)
                .toList();
    }

    /**
     * Modifies the role permissions for a given employee in the database
     *
     * @param adminDetails the administrator credentials
     * @param modifyRoleRequest the request info required
     * @return {@code EmployeeDTO} modified database details
     */
    public EmployeeDTO updateEmployeeRole(UserDetails adminDetails, ModifyRoleRequest modifyRoleRequest) {
        // STEP: Check if employee exists
        if (!employeeRepository.existsByEmailIgnoreCase(modifyRoleRequest.getEmail())) {
            logger.error(String.format("employee: %s was not found", modifyRoleRequest.getEmail()));
            throw new EmployeeNotFoundException("User not found!");
        }
        logger.info(String.format("employee: %s was found", modifyRoleRequest.getEmail()));

        //STEP: Check if requested role is valid
        if (!Role.isStringRoleValid(modifyRoleRequest.getRole())) {
            logger.error(String.format("role: %s is invalid", modifyRoleRequest.getRole()));
            throw new InvalidRoleException("Role not found!");
        }
        logger.info(String.format("role: %s is valid", modifyRoleRequest.getRole()));
        logger.info(String.format("executing operation: %s", modifyRoleRequest.getOperation()));

        if ("GRANT".equalsIgnoreCase(modifyRoleRequest.getOperation())) {
            // STEP: If operation is GRANT
            return grantEmployeeRole(modifyRoleRequest.getEmail(), modifyRoleRequest.getRole());
        } else if ("REMOVE".equalsIgnoreCase(modifyRoleRequest.getOperation())) {
            // STEP: If operation is REMOVE
            return removeEmployeeRole(adminDetails.getUsername(), modifyRoleRequest.getEmail(), modifyRoleRequest.getRole());
        } else {
            logger.warn(String.format("operation %s is invalid", modifyRoleRequest.getOperation()));
            throw new RuntimeException(String.format("operation %s is invalid", modifyRoleRequest.getOperation()));
        }

    }

    private EmployeeDTO removeEmployeeRole(String adminEmail, String employeeEmail, String requestedRole) {

        //STEP 1: Fetch employee database info
        Optional<EmployeeEntity> optionalEmployee = employeeRepository.findByEmailIgnoreCase(employeeEmail);

        optionalEmployee.ifPresent((employeeEntity) -> {
            //STEP 2: Find the required permission entity to delete
            PermissionEntity permissionEntity = employeeEntity.getPermissionEntities().stream()
                    .filter((permission) -> permission.getRole().equals(findByStringRoleNullable(requestedRole)))
                    .findFirst()
                    .orElseThrow(() -> {
                        //STEP 3: If requested role to remove is not assigned to employee
                        logger.error(String.format("employee: %s does not contain the %s role", employeeEmail, requestedRole));
                        return new RemoveRoleNotFoundException("The user does not have a role!");
                    });
            logger.info(String.format("the role to remove for employee: %s was found", employeeEmail));

            //STEP 4: Perform business rules on remove request
            checkRemoveViolations(adminEmail, employeeEmail, requestedRole, employeeEntity.getPermissionEntities());
            logger.info("successfully validated remove request");

            //STEP 5: Remove the requested role
            employeeEntity.getPermissionEntities().remove(permissionEntity);

        });
        //STEP 6: Update database and Convert entity -> dto and return
        logger.info(String.format("updating %s permissions", employeeEmail));
        return toEmployeeDTO(employeeRepository.save(optionalEmployee.orElseThrow(() -> {
            logger.error(String.format("employee: %s was not found", employeeEmail));
            return new EmployeeNotFoundException("User not found!");
        })));

    }

    private void checkRemoveViolations(String adminEmail, String employeeEmail, String requestedRole, Set<PermissionEntity> permissionEntities) {

        // check if role to remove is admin
        boolean removeAdminFlag = employeeEmail.equalsIgnoreCase(adminEmail)
                && ADMINISTRATOR.equals(Role.findByStringRoleNullable(requestedRole));
        if (removeAdminFlag) {
            logger.error(String.format("employee: %s is an administrator", employeeEmail));
            logger.error(String.format("cannot remove the %s role", requestedRole));
            throw new RemoveAdminException("Can't remove ADMINISTRATOR role!");
        }

        // check if we are deleting employee's only role
        boolean singleRoleFlag = permissionEntities.size() == 1;
        if (singleRoleFlag) {
            logger.error(String.format("employee: %s has only one role", employeeEmail));
            logger.error("employee must always have at least one role");
            throw new RemoveLastRoleException("The user must have at least one role!");
        }

    }

    private EmployeeDTO grantEmployeeRole(String email, String requestedRole) {
        //STEP 1: Fetch employee database info
        Optional<EmployeeEntity> optionalEmployee = employeeRepository.findByEmailIgnoreCase(email);

        //STEP 2: Check if grant request violates any business rules
        boolean duplicateRoleFlag = false;
        EmployeeEntity employeeEntity = optionalEmployee.orElseThrow(() -> {
            // we already check if employee existed so this case should always be false
            logger.error(String.format("employee: %s was not found", email));
            return new EmployeeNotFoundException("User not found!");
        });

        for (PermissionEntity permissionEntity : employeeEntity.getPermissionEntities()) {
            //STEP 3: Perform business rules on grant request
            checkGrantViolations(permissionEntity, email, requestedRole);
            //STEP 4: Mark if role is already granted
            if (checkGrantDuplicateRole(permissionEntity, requestedRole)) {
                logger.warn(String.format("employee: %s already contains the role: %s", email, requestedRole));
                duplicateRoleFlag = true;
                break;
            }
        }
        logger.info(String.format("successfully validated grant request for employee: %s and role: %s",
                email, requestedRole));

        //STEP 5: Skip grant operation if role already exists
        if (duplicateRoleFlag) {
            logger.warn("skipping grant operation since role is already granted");
            return toEmployeeDTO(employeeEntity);
        }
        //STEP 6: Grant employee access
            // build the requested permission
        PermissionEntity permissionEntity = PermissionEntity.builder()
                .role(Role.findByStringRoleNullable(requestedRole))
                .employeeEntity(employeeEntity)
                .build();
            // relink the permissions list with the employee
        employeeEntity.getPermissionEntities().add(permissionEntity);

        //STEP 7: Convert entity -> dto and return
        logger.info(String.format("updating %s permissions", email));
        return toEmployeeDTO(employeeRepository.save(employeeEntity));
    }

    private boolean checkGrantDuplicateRole(PermissionEntity permissionEntity, String requestedRole) {
        Role role = Role.findByStringRoleNullable(requestedRole);
        String email = permissionEntity.getEmployeeEntity().getEmail();
        // check if duplicate role request
        if (permissionEntity.getRole().equals(role)) {
            logger.warn(String.format("employee: %s already has the %s role", email, requestedRole));
            return true;
        }
        return false;
    }

    private void checkGrantViolations(PermissionEntity permissionEntity, String email, String requestedRole) {

        Role role = Role.findByStringRoleNullable(requestedRole);
        // check if admin is requesting for business role indicator
        boolean adminViolationFlag = ADMINISTRATOR.equals(permissionEntity.getRole())
                && (USER.equals(role) || ACCOUNTANT.equals(role));
        if (adminViolationFlag) {
            logger.error(String.format("employee: %s is an admin and cannot request business roles such as %s",
                    email, requestedRole));
            throw new GrantBusinessRoleException("The user cannot combine administrative and business roles!");
        }

        // check if business role is requesting for admin role indicator
        boolean businessViolationFlag = ADMINISTRATOR.equals(role)
                && (USER.equals(permissionEntity.getRole()) || ACCOUNTANT.equals(permissionEntity.getRole()));
        if (businessViolationFlag) {
            logger.error(String.format("employee: %s has business roles and cannot request admin role", email));
            throw new GrantAdminRoleException("The user cannot combine administrative and business roles!");
        }

    }

    public void deleteEmployee(String email) {
        Optional<EmployeeEntity> optionalEmployeeEntity = employeeRepository.findByEmailIgnoreCase(email);

        optionalEmployeeEntity.ifPresentOrElse((employeeEntity) -> {
            // Admin cannot be deleted
            employeeEntity.getPermissionEntities().forEach((permissionEntity) -> {
                if (ADMINISTRATOR.equals(permissionEntity.getRole())) {
                    // Respond with 400 Bad Request
                    logger.error("deletion failed because employee: %s is an admin");
                    throw new DeleteAdminException("Can't remove ADMINISTRATOR role!");
                }
            });
            // Delete employee
            employeeRepository.delete(employeeEntity);
            }, () -> {
            // Respond with 404 Not Found
            logger.error(String.format("employee: %s does not exist", email));
            throw new EmployeeNotFoundException("User not found!");
        });
    }

}
