package account.database.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payrolls")
public class PayrollEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "salary", nullable = false)
    private Long salary;

    @Column(name = "period", nullable = false)
    private String period;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employeeEntity;

}
