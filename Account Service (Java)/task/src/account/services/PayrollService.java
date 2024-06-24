package account.services;

import account.database.entities.PayrollEntity;
import account.database.entities.EmployeeEntity;
import account.database.repositories.PayrollRepository;
import account.database.repositories.EmployeeRepository;
import account.exceptions.EmailDoesNotExistException;
import account.exceptions.PeriodAlreadyExistsException;
import account.exceptions.PeriodDoesNotExistException;
import account.models.dto.PayrollDTO;
import account.models.requests.Payroll;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PayrollService {

    private final PayrollRepository payrollRepository;

    private final EmployeeRepository employeeRepository;

    private final Logger logger = LoggerFactory.getLogger(PayrollService.class);

    @Autowired
    public PayrollService(PayrollRepository payrollRepository, EmployeeRepository employeeRepository) {
        this.payrollRepository = payrollRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * This method is used to map payrolls with employees and add them in the database
     * @param payrolls the list of payrolls
     * */
    public void addPayrolls(List<Payroll> payrolls) {
        //todo: STEP 1: Check if list is not empty

        // STEP 2: Fetch the impacted employees
        Map<String, EmployeeEntity> employees = findImpactedEmployees(payrolls);
        logger.info("found the impacted employees");

        payrolls.forEach((payroll) -> {
            EmployeeEntity employee = employees.get(payroll.getEmail());
            // STEP 3: Check if each employee exists
            if (Objects.isNull(employee)) {
                logger.error(String.format("employee with email = %s is not registered", payroll.getEmail()));
                throw new EmailDoesNotExistException("email not found because employee is not registered");
            }

            logger.info(String.format("employee = %s was found", payroll.getEmail()));

            // STEP 4: Check if period already exists for employee
            employee.getPayrollEntities().forEach((currentPayroll) -> {
                if (Objects.equals(currentPayroll.getPeriod(), payroll.getPeriod())) {
                    logger.error(String.format("employee with email %s already has a payroll period %s",
                            payroll.getEmail(), payroll.getPeriod()));
                    throw new PeriodAlreadyExistsException("payroll period already exists");
                }
            });

            logger.info(String.format("payroll period = %s is unique", payroll.getPeriod()));
        });

        // STEP 5: Save payroll data
        savePayrolls(payrolls, employees);
    }

    /**
     * This method is used to save the payrolls in the database
     * @param payrolls the payroll info to save
     * @param employees the map of email/employee pairs */
    @Transactional
    private void savePayrolls(List<Payroll> payrolls, Map<String, EmployeeEntity> employees) {
        // set the employee entity into payroll entities
        List<PayrollEntity> payrollEntities = payrolls.stream().map((payroll) -> PayrollEntity.builder()
                .salary(payroll.getSalary())
                .period(payroll.getPeriod())
                .employeeEntity(employees.get(payroll.getEmail()))
                .build()).toList();
        // set payroll entities into the employee entity
        for (PayrollEntity payrollEntity : payrollEntities) {
            EmployeeEntity employeeEntity = payrollEntity.getEmployeeEntity();
            employeeEntity.getPayrollEntities().add(payrollEntity);
        }

        employeeRepository.saveAll(employees.values());
    }

    /**
     * This method is used to find impacted employees whose payroll info needs to be updated. If an employee is null,
     * then the employee does not exist.
     * @param payrolls list of employee's payrolls
     * @return map with email/employee pairs
     * */
    private Map<String, EmployeeEntity> findImpactedEmployees(List<Payroll> payrolls) {
        Map<String, EmployeeEntity> employees = new HashMap<>();
        for (Payroll payroll : payrolls) {
            if (!employees.containsKey(payroll.getEmail())) {
                Optional<EmployeeEntity> optional = employeeRepository.findByEmailIgnoreCase(payroll.getEmail());
                employees.put(payroll.getEmail(), optional.orElse(null));
            }
        }
        return employees;
    }

    /**
     * This method is used to modify an existing payroll
     * @param payroll the payroll whose salary is expected to update
     * */
    public void modifyPayrollSalary(Payroll payroll) {
        // STEP 1: Fetch the impacted employee
        EmployeeEntity impactedEmployee = employeeRepository.findByEmailIgnoreCase(payroll.getEmail()).orElse(null);

        // STEP 2: Check if employee is registered
        if (Objects.isNull(impactedEmployee)) {
            logger.error(String.format("employee with email = %s is not registered", payroll.getEmail()));
            throw new EmailDoesNotExistException("email not found because employee is not registered");
        }

        logger.info(String.format("employee = %s was found", payroll.getEmail()));

        // STEP 3: Fetch the impacted payroll
        PayrollEntity impactedPayroll = impactedEmployee.getPayrollEntities().stream()
                .filter((payrollEntity) -> Objects.equals(payrollEntity.getPeriod(), payroll.getPeriod()))
                .findFirst().orElse(null);

        // STEP 4: Check if period does not exist for the employee
        if (Objects.isNull(impactedPayroll)) {
            logger.error(String.format("payroll period = %s for employee = %s cannot be modified since it does not exist",
                    payroll.getPeriod(), payroll.getEmail()));
            throw new PeriodDoesNotExistException("payroll period to modify does not exist");
        }

        logger.info(String.format("payroll period = %s was found", payroll.getPeriod()));

        // STEP 5: Update modified salary
        updatePayrollSalary(impactedPayroll, payroll);

    }

    /**
     * This method is used to update payroll salary in the database
     * @param modifiedPayroll the modified payroll to update
     * @param impactedPayroll the associated payroll entity
     * */
    @Transactional
    private void updatePayrollSalary(PayrollEntity impactedPayroll, Payroll modifiedPayroll) {
        impactedPayroll.setSalary(modifiedPayroll.getSalary());
        payrollRepository.save(impactedPayroll);
    }

    /**
     * This method is used to fetch payroll data for a given period
     * @param userDetails the employee details
     * @param period the desired period
     * @return the requested payroll data; returns null if payroll period does not exist
     * */
    public PayrollDTO getPayroll(UserDetails userDetails, String period) {
        // check if period has invalid format
        if (!period.matches("^(0?[1-9]|1[0-2])-(\\d{4})$")) {
            final String message = String.format("period = %s is not in mm-YYYY format", period);
            logger.error(message);
            throw new PeriodDoesNotExistException(message);
        }

        // find the employee information
        EmployeeEntity employeeEntity = employeeRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> {
                    // should never execute
                    logger.error("could not find user data despite passing authentication");
                    return new UsernameNotFoundException("could not find user data despite passing authentication");
                });
        // extract payroll info and return the required payroll period
        return employeeEntity.getPayrollEntities().stream()
                .filter((payrollEntity) -> isPeriodEquals(payrollEntity.getPeriod(), period))
                .map(this::toPayrollDTO).findFirst().orElse(null);
    }

    /**
     * This method is used to compare periods regardless if leading zeros exist
     * @param storedPeriod the period from the database and source of truth
     * @param requestPeriod the requested period to compare with
     * @return boolean determining if both periods are same
     * */
    private boolean isPeriodEquals(String storedPeriod, String requestPeriod) {
        if (storedPeriod.length() == requestPeriod.length())
            return Objects.equals(storedPeriod, requestPeriod);

        if (storedPeriod.length() > requestPeriod.length()) {
            return Objects.equals(storedPeriod.substring(1), requestPeriod);
        } else {
            return Objects.equals(storedPeriod, requestPeriod.substring(1));
        }
    }

    /**
     * This method is used to fetch all payroll data related to an employee
     * @param userDetails the employee details
     * @return the list of payroll information of an employee sorted in descending order;
     * returns empty list if no payroll data exists
     * */
    public List<PayrollDTO> getPayrolls(UserDetails userDetails) {
        // find the employee information
        EmployeeEntity employeeEntity = employeeRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> {
                    // should never execute
                    logger.error("could not find user data despite passing authentication");
                    return new UsernameNotFoundException("could not find user data despite passing authentication");
                });
        // convert and return the list of payroll data
        if (Objects.nonNull(employeeEntity.getPayrollEntities()) && !employeeEntity.getPayrollEntities().isEmpty()) {
            return new ArrayList<>(employeeEntity.getPayrollEntities().stream().map(this::toPayrollDTO).toList());
        } else {
            return new ArrayList<>();
        }
    }

    private PayrollDTO toPayrollDTO(PayrollEntity entity) {

        return PayrollDTO.builder()
                .firstName(entity.getEmployeeEntity().getFirstName())
                .lastName(entity.getEmployeeEntity().getLastName())
                .period(entity.getPeriod())
                .salary(entity.getSalary())
                .build();
    }
}
