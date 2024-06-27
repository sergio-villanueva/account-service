package account.models.requests;

import account.validations.AcmeEmail;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class Payroll {
    @JsonProperty("employee")
    @NotBlank @AcmeEmail
    private String email;
    @JsonProperty("period")
    @Pattern(regexp = "^(0?[1-9]|1[0-2])-(\\d{4})$")
    private String period;
    @JsonProperty("salary")
    @Min(0)
    private long salary;
}
