package account.database.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee")
public class EmployeeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "created_timestamp", nullable = false)
    private LocalDateTime created;

    @Column(name = "updated_timestamp")
    private LocalDateTime updated;

    @OneToMany(mappedBy = "employeeEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PayrollEntity> payrollEntities;

}
