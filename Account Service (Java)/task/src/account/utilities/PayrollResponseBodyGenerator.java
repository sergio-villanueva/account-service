package account.utilities;

import account.models.dto.PayrollDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
public class PayrollResponseBodyGenerator {

    private final Logger logger = LoggerFactory.getLogger(PayrollResponseBodyGenerator.class);

    public Object addPayrollsResponseBody() {
        return AddUpdatePayrollsResponseBody.builder()
                .status("Added successfully!")
                .build();
    }

    public Object modifyPayrollResponseBody() {
        return AddUpdatePayrollsResponseBody.builder()
                .status("Updated successfully!")
                .build();
    }

    /**
     * This method is used to build the required response for the single payroll flow;
     * @param dto the payroll data to respond with
     * @return response object with required data; if no payroll data was found then return empty response
     * */
    public Object getPayrollResponseBody(PayrollDTO dto) {
        if (Objects.nonNull(dto)) {
            return toGetPayrollResponseBody(dto);
        } else {
            return new EmptyJSONBody();
        }
    }

    /**
     * This method is used to build the required response for the multi payroll flow;
     * */
    public Object getPayrollResponseBody(List<PayrollDTO> dtos) {
        // sort the list of payrolls in descending order by payroll period
        dtos.sort((dto1, dto2) -> {
            final DateFormat dateFormat = new SimpleDateFormat("MM-yyyy");
            try {
                Date date1 = dateFormat.parse(dto1.getPeriod());
                Date date2 = dateFormat.parse(dto2.getPeriod());
                return date2.compareTo(date1);

            } catch (ParseException e) {
                logger.error("period format (mm-YYYY) validation has failed");
                throw new RuntimeException(e);
            }
        });
        // convert each dto -> response
        return dtos.stream().map(this::toGetPayrollResponseBody).toList();
    }

    private GetPayrollResponseBody toGetPayrollResponseBody(PayrollDTO payrollDTO) {
        return GetPayrollResponseBody.builder()
                .firstName(payrollDTO.getFirstName())
                .lastName(payrollDTO.getLastName())
                .period(payrollDTO.getPeriod())
                .salary(toSalaryFormat(payrollDTO.getSalary()))
                .build();
    }

    private String toSalaryFormat(Long salary) {
        Long dollars = salary / 100;
        Long cents = salary % 100;
        return String.format("%d dollar(s) %d cent(s)", dollars, cents);
    }

    @Data
    @Builder
    private static class AddUpdatePayrollsResponseBody {
        private String status;
    }

    @Data
    @Builder
    private static class GetPayrollResponseBody {
        @JsonProperty("name")
        private String firstName;
        @JsonProperty("lastname")
        private String lastName;
        @JsonProperty("period")
        private String period;
        @JsonProperty("salary")
        private String salary;
    }

    @JsonSerialize
    private static class EmptyJSONBody {

    }
}
