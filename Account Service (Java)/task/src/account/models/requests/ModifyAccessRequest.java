package account.models.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifyAccessRequest {
    @JsonProperty("user")
    @NotBlank
    private String email;
    @JsonProperty("operation")
    @NotBlank @Pattern(regexp = "LOCK|UNLOCK")
    private String operation;
}
