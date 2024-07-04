package account.models.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ModifyRoleRequest {
    @JsonProperty("user")
    @NotBlank
    private String email;
    @JsonProperty("role")
    @NotBlank
    private String role;
    @JsonProperty("operation")
    @NotBlank @Pattern(regexp = "GRANT|REMOVE")
    private String operation;
}
