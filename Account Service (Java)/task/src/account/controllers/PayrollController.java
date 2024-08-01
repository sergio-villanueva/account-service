package account.controllers;

import account.models.dto.PayrollDTO;
import account.models.requests.Payroll;
import account.services.PayrollService;
import account.view_generators.PayrollResponseBodyGenerator;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
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
public class PayrollController {

    private final Logger logger = LoggerFactory.getLogger(PayrollController.class);

    private final PayrollResponseBodyGenerator responseBodyGenerator;

    private final PayrollService payrollService;

    private final Validator validator;

    @Autowired
    public PayrollController(PayrollResponseBodyGenerator responseBodyGenerator, PayrollService payrollService, Validator validator) {
        this.responseBodyGenerator = responseBodyGenerator;
        this.payrollService = payrollService;
        this.validator = validator;
    }

    /**
     * The journey used for uploading payrolls
     * */
    @PostMapping("/acct/payments")
    @ResponseStatus(code = HttpStatus.OK)
    public Object addPayrolls(@Valid @RequestBody List<Payroll> payrolls) {
        logger.info("start add payrolls journey");
        payrolls.forEach((validator::validate));
        logger.info("validation successful");
        payrollService.addPayrolls(payrolls);
        logger.info("successfully saved payrolls");
        return responseBodyGenerator.addPayrollsResponseBody();
    }

    /**
     * The journey used for modifying payroll salary
     * */
    @PutMapping("/acct/payments")
    @ResponseStatus(code = HttpStatus.OK)
    public Object modifyPayrollSalary(@Valid @RequestBody Payroll payroll) {
        logger.info("validation successful so start modify payroll salary journey");
        payrollService.modifyPayrollSalary(payroll);
        logger.info("successfully updated payroll salary");
        return responseBodyGenerator.modifyPayrollResponseBody();
    }

    /**
     * The journey used for fetching payroll information for a given employee
     * */
    @GetMapping("/empl/payment")
    @ResponseStatus(code = HttpStatus.OK)
    public Object getPayroll(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam(value = "period", required = false) String period) {
        logger.info("start get payroll journey");
        if (StringUtils.isNotBlank(period)) {
            // deliver payroll data for the given period only
            PayrollDTO payrollDTO = payrollService.getPayroll(userDetails, period);
            logger.info("payroll retrieved successfully");
            return responseBodyGenerator.getPayrollResponseBody(payrollDTO);
        } else {
            // deliver all payroll data of employee when period not given
            List<PayrollDTO> payrollDTOList = payrollService.getPayrolls(userDetails);
            logger.info("payroll list retrieved successfully");
            return responseBodyGenerator.getPayrollResponseBody(payrollDTOList);
        }

    }
}
