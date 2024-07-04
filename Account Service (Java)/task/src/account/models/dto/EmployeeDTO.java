package account.models.dto;

import account.utilities.Role;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class EmployeeDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Set<Role> roles;
}
