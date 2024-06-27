package account.controllers;

import account.models.requests.ChangePasswordRequest;
import account.models.requests.Registration;
import account.models.dto.EmployeeDTO;
import account.services.EmployeeService;
import account.utilities.RegistrationResponseBodyGenerator;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") @Validated
public class EmployeeController {

    private final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private final EmployeeService employeeService;
    private final RegistrationResponseBodyGenerator responseBodyGenerator;

    @Autowired
    public EmployeeController(EmployeeService employeeService, RegistrationResponseBodyGenerator responseBodyGenerator) {
        this.employeeService = employeeService;
        this.responseBodyGenerator = responseBodyGenerator;
    }

    /**
     * The journey used for registering a user to the account service platform
     * */
    @PostMapping("/signup")
    @ResponseStatus(code = HttpStatus.OK)
    public Object register(@Valid @RequestBody Registration registration) {
        logger.info("start registration journey");
        EmployeeDTO dto = employeeService.createUser(registration);
        logger.info("registration details saved successfully");
        return responseBodyGenerator.buildRegistrationResponseBody(dto);
    }

    /**
     * The journey used for changing passwords
     * */
    @PostMapping("/changepass")
    @ResponseStatus(code = HttpStatus.OK)
    public Object changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                 @Valid @RequestBody ChangePasswordRequest request) {
        logger.info("start change password journey");
        EmployeeDTO employeeDTO = employeeService.changePassword(userDetails, request);
        logger.info("new password successfully saved");
        return responseBodyGenerator.buildChangePasswordResponseBody(employeeDTO);
    }


}
