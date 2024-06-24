package account.models.requests;

import account.validations.ValidationMessages;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @JsonProperty("new_password")
    @NotBlank(message = ValidationMessages.MISSING_PASSWORD) @Size(min = 12, message = ValidationMessages.MIN_PASS_LENGTH)
    private String newPassword;
}
