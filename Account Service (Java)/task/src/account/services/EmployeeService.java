package account.services;

import account.database.entities.CommonEventEntity;
import account.database.entities.EmployeeEntity;
import account.database.entities.PermissionEntity;
import account.database.repositories.CommonEventRepository;
import account.database.repositories.EmployeeRepository;
import account.exceptions.*;
import account.models.dto.EmployeeDTO;
import account.models.dto.EventDTO;
import account.models.requests.ChangePasswordRequest;
import account.models.requests.ModifyAccessRequest;
import account.models.requests.ModifyRoleRequest;
import account.models.requests.Registration;
import account.publishers.JourneyEventPublisher;
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

    private final JourneyEventPublisher journeyEventPublisher;

    private final CommonEventRepository commonEventRepository;

    private final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, CompromisedPasswordChecker compromisedPasswordChecker, PasswordEncoder passwordEncoder, JourneyEventPublisher journeyEventPublisher, CommonEventRepository commonEventRepository) {
        this.employeeRepository = employeeRepository;
        this.compromisedPasswordChecker = compromisedPasswordChecker;
        this.passwordEncoder = passwordEncoder;
        this.journeyEventPublisher = journeyEventPublisher;
        this.commonEventRepository = commonEventRepository;
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
                .lockFlag(Boolean.FALSE)
                .payrollEntities(new ArrayList<>())
                .permissionEntities(new HashSet<>(Set.of(permissionEntity)))
                .build();

        permissionEntity.setEmployeeEntity(employeeEntity);

        // Step 5: Save employee in database and return DTO
        EmployeeEntity savedEntity = employeeRepository.save(employeeEntity);
        savedEntity.setEmail(originalEmail);

        // Step 6: Publish create employee event
        journeyEventPublisher.publishCreateEmployeeEvent(registration.getEmail().toLowerCase());

        return toEmployeeDTO(savedEntity);
    }

    /** Changes the password for a given employee
     * @param userDetails the employee's authenticated user details
     * @param request the request object containing the new password
     * @return {@code EmployeeDTO}
     * */
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

        // STEP 5: Publish change password event
        journeyEventPublisher.publishChangePasswordEvent(userDetails.getUsername());

        // STEP 6: Return user data to form response
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

    /** Fetches list of all employees
     * @return {@code List<EmployeeDTO>}
     * */
    public List<EmployeeDTO> retrieveEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::toEmployeeDTO)
                .toList();
    }

    /** Fetches list of all events sorted in ascending order by id
     * @return {@code List<EventDTO>}
     * */
    public List<EventDTO> retrieveEvents() {
        return commonEventRepository.findAll().stream()
                .sorted(Comparator.comparing(CommonEventEntity::getId))
                .map(this::toEventDTO)
                .toList();
    }

    private EventDTO toEventDTO(CommonEventEntity entity) {
        return EventDTO.builder()
                .date(entity.getCreated())
                .eventType(entity.getEventType())
                .subject(entity.getSubject())
                .object(entity.getObject())
                .path(entity.getPath())
                .build();
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
            return grantEmployeeRole(adminDetails.getUsername(), modifyRoleRequest.getEmail(), modifyRoleRequest.getRole());
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
        //STEP 6: Update database and Convert entity -> dto
        logger.info(String.format("updating %s permissions", employeeEmail));
        EmployeeDTO dto = toEmployeeDTO(employeeRepository.save(optionalEmployee.orElseThrow(() -> {
            logger.error(String.format("employee: %s was not found", employeeEmail));
            return new EmployeeNotFoundException("User not found!");
        })));
        // STEP 7: Publish remove role event
        journeyEventPublisher.publishRemoveRoleEvent(adminEmail, employeeEmail, requestedRole);
        return dto;

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

    private EmployeeDTO grantEmployeeRole(String adminEmail, String employeeEmail, String requestedRole) {
        //STEP 1: Fetch employee database info
        Optional<EmployeeEntity> optionalEmployee = employeeRepository.findByEmailIgnoreCase(employeeEmail);

        //STEP 2: Check if grant request violates any business rules
        boolean duplicateRoleFlag = false;
        EmployeeEntity employeeEntity = optionalEmployee.orElseThrow(() -> {
            // we already check if employee existed so this case should always be false
            logger.error(String.format("employee: %s was not found", employeeEmail));
            return new EmployeeNotFoundException("User not found!");
        });

        for (PermissionEntity permissionEntity : employeeEntity.getPermissionEntities()) {
            //STEP 3: Perform business rules on grant request
            checkGrantViolations(permissionEntity, employeeEmail, requestedRole);
            //STEP 4: Mark if role is already granted
            if (checkGrantDuplicateRole(permissionEntity, requestedRole)) {
                logger.warn(String.format("employee: %s already contains the role: %s", employeeEmail, requestedRole));
                duplicateRoleFlag = true;
                break;
            }
        }
        logger.info(String.format("successfully validated grant request for employee: %s and role: %s",
                employeeEmail, requestedRole));

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

        //STEP 7: Convert entity -> dto
        logger.info(String.format("updating %s permissions", employeeEmail));
        EmployeeDTO dto = toEmployeeDTO(employeeRepository.save(employeeEntity));

        // STEP 8: Publish grant role event
        journeyEventPublisher.publishGrantRoleEvent(adminEmail, employeeEmail, requestedRole);
        return dto;
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
        boolean adminViolationFlag = Role.isAdminRole(permissionEntity.getRole())
                && Role.isBusinessRole(role);
        if (adminViolationFlag) {
            logger.error(String.format("employee: %s is an admin and cannot request business roles such as %s",
                    email, requestedRole));
            throw new GrantBusinessRoleException("The user cannot combine administrative and business roles!");
        }

        // check if business role is requesting for admin role indicator
        boolean businessViolationFlag = Role.isAdminRole(role)
                && Role.isBusinessRole(permissionEntity.getRole());
        if (businessViolationFlag) {
            logger.error(String.format("employee: %s has business roles and cannot request admin role", email));
            throw new GrantAdminRoleException("The user cannot combine administrative and business roles!");
        }

    }

    /** Modifies the access for a given employee
     * @param modifyAccessRequest the request containing employee info
     * */
    public EmployeeDTO modifyEmployeeAccess(ModifyAccessRequest modifyAccessRequest) {
        Optional<EmployeeEntity> optionalEmployee = employeeRepository
                .findByEmailIgnoreCase(modifyAccessRequest.getEmail());
        optionalEmployee.ifPresent((employeeEntity -> {

            if ("LOCK".equalsIgnoreCase(modifyAccessRequest.getOperation())) {
                logger.info(String.format("locking employee %s",
                        modifyAccessRequest.getEmail()));
                // check if request is to lock admin
                checkAccessViolations(employeeEntity.getPermissionEntities());
                // lock employee account
                employeeRepository.updateLockFlagByEmailIgnoreCase(Boolean.TRUE, employeeEntity.getEmail());
                // publish lock employee event
                journeyEventPublisher.publishLockEmployeeAccessEvent(employeeEntity.getEmail());

            } else if ("UNLOCK".equalsIgnoreCase(modifyAccessRequest.getOperation())) {
                logger.info(String.format("unlocking employee %s",
                        modifyAccessRequest.getEmail()));
                // unlock employee account
                employeeRepository.updateLockFlagByEmailIgnoreCase(Boolean.FALSE, employeeEntity.getEmail());
                // publish unlock employee event
                journeyEventPublisher.publishUnlockEmployeeAccessEvent(employeeEntity.getEmail());

            } else {
                logger.warn(String.format("access operation %s is invalid for employee %s ",
                        modifyAccessRequest.getOperation(),
                        modifyAccessRequest.getEmail()));
            }

        }));

        return toEmployeeDTO(optionalEmployee.orElseThrow(() -> {
            logger.error(String.format("employee %s was not found in database", modifyAccessRequest.getEmail()));
            return new EmployeeNotFoundException("User not found!");
        }));
    }

    /** Modifies the access for a given employee in an async friendly manner
     * @param modifyAccessRequest the request containing employee info
     * @param path the request uri tha was executed
     * */
    public void modifyEmployeeAccessAsync(ModifyAccessRequest modifyAccessRequest, String path) {
        Optional<EmployeeEntity> optionalEmployee = employeeRepository
                .findByEmailIgnoreCase(modifyAccessRequest.getEmail());
        optionalEmployee.ifPresentOrElse((employeeEntity -> {

            if ("LOCK".equalsIgnoreCase(modifyAccessRequest.getOperation())) {
                logger.info(String.format("locking employee %s",
                        modifyAccessRequest.getEmail()));
                // check if request is to lock admin
                checkAccessViolations(employeeEntity.getPermissionEntities());
                // lock employee account
                employeeRepository.updateLockFlagByEmailIgnoreCase(Boolean.TRUE, employeeEntity.getEmail());
                // publish lock employee event
                journeyEventPublisher.publishLockEmployeeAccessAsyncEvent(employeeEntity.getEmail(), path);

            } else if ("UNLOCK".equalsIgnoreCase(modifyAccessRequest.getOperation())) {
                logger.info(String.format("unlocking employee %s",
                        modifyAccessRequest.getEmail()));
                // unlock employee account
                employeeRepository.updateLockFlagByEmailIgnoreCase(Boolean.FALSE, employeeEntity.getEmail());
                // publish unlock employee event
                journeyEventPublisher.publishUnlockEmployeeAccessAsyncEvent(employeeEntity.getEmail(), path);
            } else {
                logger.warn(String.format("access operation %s is invalid for employee %s ",
                        modifyAccessRequest.getOperation(),
                        modifyAccessRequest.getEmail()));
            }

        }), () -> {
            // employee does not exist scenario
            logger.error(String.format("employee %s was not found in database", modifyAccessRequest.getEmail()));
            throw new EmployeeNotFoundException("User not found!");
        });
    }

    private void checkAccessViolations(Set<PermissionEntity> permissionEntities) {
        for (PermissionEntity permissionEntity : permissionEntities) {
            if (Role.isAdminRole(permissionEntity.getRole())) {
                throw new ModifyAdminAccessException("Can't lock the ADMINISTRATOR!");
            }
        }
    }

    /** Deletes a given employee
     * @param adminEmail the email of the administrator
     * @param employeeEmail the email of the employee to be deleted
     * */
    public void deleteEmployee(String adminEmail, String employeeEmail) {
        Optional<EmployeeEntity> optionalEmployeeEntity = employeeRepository.findByEmailIgnoreCase(employeeEmail);

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

            // Publish delete employee event
            journeyEventPublisher.publishDeleteEmployeeEvent(adminEmail, employeeEmail);

            }, () -> {
            // Respond with 404 Not Found
            logger.error(String.format("employee: %s does not exist", employeeEmail));
            throw new EmployeeNotFoundException("User not found!");
        });
    }

}
