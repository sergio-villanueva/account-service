package account.models.requests;

import account.validations.AcmeEmail;
import account.validations.ValidationMessages;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Registration {
    @JsonProperty("name")
    @NotBlank(message = ValidationMessages.MISSING_NAME)
    private String firstName;
    @JsonProperty("lastname")
    @NotBlank(message = ValidationMessages.MISSING_LASTNAME)
    private String lastName;
    @JsonProperty("email")
    @NotBlank(message = ValidationMessages.MISSING_EMAIL) @AcmeEmail()
    private String email;
    @JsonProperty("password")
    @NotBlank(message = ValidationMessages.MISSING_PASSWORD) @Size(min = 12, message = ValidationMessages.MIN_PASS_LENGTH)
    private String password;
}
