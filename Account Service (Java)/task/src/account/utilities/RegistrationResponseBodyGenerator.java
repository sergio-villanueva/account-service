package account.utilities;

import account.models.dto.EmployeeDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
public class RegistrationResponseBodyGenerator {
    public Object buildRegistrationResponseBody(EmployeeDTO dto) {
        return RegistrationResponseBody.builder()
                .id(dto.getId())
                .name(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .build();
    }

    public Object buildChangePasswordResponseBody(EmployeeDTO employeeDTO) {
        return ChangePasswordResponseBody.builder()
                .email(employeeDTO.getEmail())
                .status("The password has been updated successfully")
                .build();
    }

    @Data
    @Builder
    private static class RegistrationResponseBody {
        @JsonProperty("id")
        private Long id;
        @JsonProperty("name")
        private String name;
        @JsonProperty("lastname")
        private String lastName;
        @JsonProperty("email")
        private String email;
    }

    @Data
    @Builder
    private static class ChangePasswordResponseBody {
        @JsonProperty("email")
        private String email;
        @JsonProperty("status")
        private String status;
    }
}
