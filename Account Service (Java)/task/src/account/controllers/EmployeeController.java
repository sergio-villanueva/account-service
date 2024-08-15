package account.controllers;

import account.models.dto.EmployeeDTO;
import account.models.dto.EventDTO;
import account.models.requests.ChangePasswordRequest;
import account.models.requests.ModifyAccessRequest;
import account.models.requests.ModifyRoleRequest;
import account.models.requests.Registration;
import account.services.EmployeeService;
import account.generators.EmployeeResponseBodyGenerator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api") @Validated
public class EmployeeController {

    private final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private final EmployeeService employeeService;
    private final EmployeeResponseBodyGenerator responseBodyGenerator;

    @Autowired
    public EmployeeController(EmployeeService employeeService, EmployeeResponseBodyGenerator responseBodyGenerator) {
        this.employeeService = employeeService;
        this.responseBodyGenerator = responseBodyGenerator;
    }

    /**
     * The journey used for registering an employee to the account service platform
     * */
    @PostMapping("/auth/signup")
    @ResponseStatus(code = HttpStatus.OK)
    public Object register(@Valid @RequestBody Registration registration) {
        logger.info(String.format("start registration journey for employee: %s", registration.getEmail()));
        EmployeeDTO dto = employeeService.createEmployee(registration);
        logger.info("registration details saved successfully");
        return responseBodyGenerator.buildRegistrationResponseBody(dto);
    }

    /**
     * The journey used for changing passwords
     * */
    @PostMapping("/auth/changepass")
    @ResponseStatus(code = HttpStatus.OK)
    public Object changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                 @Valid @RequestBody ChangePasswordRequest request) {
        logger.info(String.format("start change password journey for employee: %s", userDetails.getUsername()));
        EmployeeDTO employeeDTO = employeeService.changePassword(userDetails, request);
        logger.info("new password successfully saved");
        return responseBodyGenerator.buildChangePasswordResponseBody(employeeDTO);
    }

    /**
     * The journey used to obtain information on all employees
     * */
    @GetMapping("/admin/user/")
    @ResponseStatus(code = HttpStatus.OK)
    public Object retrieveAllEmployees() {
        logger.info("start retrieve all employees journey");
        List<EmployeeDTO> employeeDTOS = employeeService.retrieveEmployees();
        logger.info("successfully retrieved all employee information");
        return responseBodyGenerator.buildRetrieveEmployeesResponseBody(employeeDTOS);
    }

    /**
     * The journey used to obtain all events for an auditor
     * */
    @GetMapping("/security/events/")
    @ResponseStatus(code = HttpStatus.OK)
    public Object retrieveAllEvents() {
        logger.info("start retrieve all events journey");
        List<EventDTO> eventDTOS = employeeService.retrieveEvents();
        logger.info("successfully retrieved all event information");
        return responseBodyGenerator.buildRetrieveEventsResponseBody(eventDTOS);
    }

    /**
     * The journey used by an administrator to modify the role of an employee
     * */
    @PutMapping("/admin/user/role")
    @ResponseStatus(code = HttpStatus.OK)
    public Object modifyRole(@AuthenticationPrincipal UserDetails adminDetails,
                             @Valid @RequestBody ModifyRoleRequest modifyRoleRequest) {
        logger.info("start modify employee role journey");
        EmployeeDTO employeeDTO = employeeService.updateEmployeeRole(adminDetails, modifyRoleRequest);
        logger.info(String.format("successfully updated employee role with %s operation", modifyRoleRequest.getOperation()));
        return responseBodyGenerator.buildModifyRoleResponseBody(employeeDTO);
    }

    @PutMapping("/admin/user/access")
    @ResponseStatus(code = HttpStatus.OK)
    public Object modifyAccess(@AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ModifyAccessRequest modifyAccessRequest) {
        logger.info("start modify employee access journey");
        EmployeeDTO employeeDTO = employeeService.modifyEmployeeAccess(modifyAccessRequest, userDetails);
        logger.info(String.format("successfully modified employee access for %s", modifyAccessRequest.getEmail()));
        return responseBodyGenerator.buildModifyAccessResponseBody(modifyAccessRequest, employeeDTO);
    }

    /**
     * The journey used to delete an employee
     * */
    @DeleteMapping("/admin/user/{email}")
    @ResponseStatus(code = HttpStatus.OK)
    public Object delete(@AuthenticationPrincipal UserDetails userDetails,
                         @PathVariable("email") @NotBlank String employeeEmail) {
        logger.info("start delete employee journey");
        employeeService.deleteEmployee(userDetails.getUsername(), employeeEmail);
        logger.info(String.format("successfully deleted employee: %s", employeeEmail));
        return responseBodyGenerator.buildDeleteResponseBody(employeeEmail);
    }
}
