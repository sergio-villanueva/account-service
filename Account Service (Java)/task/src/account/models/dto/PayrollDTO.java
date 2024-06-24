package account.models.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayrollDTO {
    private String firstName;
    private String lastName;
    private String period;
    private Long salary;
}
