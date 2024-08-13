package account.generators;

import account.models.dto.EmployeeDTO;
import account.models.dto.EventDTO;
import account.models.requests.ModifyAccessRequest;
import account.utilities.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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

    public Object buildRetrieveEmployeesResponseBody(List<EmployeeDTO> employeeDTOS) {
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

    public Object buildModifyAccessResponseBody(ModifyAccessRequest modifyAccessRequest) {
        String operation = "LOCK".equalsIgnoreCase(modifyAccessRequest.getOperation()) ?
                "locked" : "UNLOCK".equalsIgnoreCase(modifyAccessRequest.getOperation()) ?
                "unlocked" : null;
        return ModifyAccessResponseBody.builder()
                .status(String.format("User %s %s!",
                        modifyAccessRequest.getEmail(),
                        operation))
                .build();
    }

    public Object buildRetrieveEventsResponseBody(List<EventDTO> eventDTOS) {
        return eventDTOS.stream()
                .map(this::toEventResponseItem)
                .toArray();
    }

    private EventResponseItem toEventResponseItem(EventDTO dto) {
        return EventResponseItem.builder()
                .date(dto.getDate())
                .eventType(dto.getEventType())
                .subject(dto.getSubject())
                .object(dto.getObject())
                .path(dto.getPath())
                .build();
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

    @Data
    @Builder
    private static class ModifyAccessResponseBody {
        @JsonProperty("status")
        private String status;
    }

    @Data
    @Builder
    private static class EventResponseItem {
        @JsonProperty("date")
        private LocalDateTime date;
        @JsonProperty("action")
        private String eventType;
        @JsonProperty("subject")
        private String subject;
        @JsonProperty("object")
        private String object;
        @JsonProperty("path")
        private String path;
    }
}
