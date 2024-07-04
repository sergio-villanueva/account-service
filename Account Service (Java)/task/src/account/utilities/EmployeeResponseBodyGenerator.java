package account.utilities;

import account.models.dto.EmployeeDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class EmployeeResponseBodyGenerator {
    public Object buildRegistrationResponseBody(EmployeeDTO dto) {
        return toRegistrationResponseBody(dto);
    }

    public Object buildChangePasswordResponseBody(EmployeeDTO employeeDTO) {
        return ChangePasswordResponseBody.builder()
                .email(employeeDTO.getEmail())
                .status("The password has been updated successfully")
                .build();
    }

    public Object buildRetrieveAllResponseBody(List<EmployeeDTO> employeeDTOS) {
        return employeeDTOS.stream()
                .sorted(Comparator.comparing(EmployeeDTO::getId))
                .map(this::toRegistrationResponseBody)
                .toList();
    }

    public Object buildDeleteResponseBody(String email) {
        return DeleteResponseBody.builder()
                .email(email)
                .status("Deleted successfully!")
                .build();
    }

    public Object buildModifyRoleResponseBody(EmployeeDTO employeeDTO) {
        return toRegistrationResponseBody(employeeDTO);
    }

    private RegistrationResponseBody toRegistrationResponseBody(EmployeeDTO dto) {
        return RegistrationResponseBody.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .roles(new TreeSet<>(dto.getRoles().stream().map(Role::getAuthority).collect(Collectors.toSet())))
                .build();
    }

    @Data
    @Builder
    private static class RegistrationResponseBody {
        @JsonProperty("id")
        private Long id;
        @JsonProperty("name")
        private String firstName;
        @JsonProperty("lastname")
        private String lastName;
        @JsonProperty("email")
        private String email;
        @JsonProperty("roles")
        private SortedSet<String> roles;
    }

    @Data
    @Builder
    private static class ChangePasswordResponseBody {
        @JsonProperty("email")
        private String email;
        @JsonProperty("status")
        private String status;
    }

    @Data
    @Builder
    private static class DeleteResponseBody {
        @JsonProperty("user")
        private String email;
        @JsonProperty("status")
        private String status;
    }
}
