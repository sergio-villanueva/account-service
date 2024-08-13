package account.database.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employees")
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

    @Builder.Default
    @Column(name = "lock_flag", nullable = false)
    private Boolean lockFlag = false;

    @OneToMany(mappedBy = "employeeEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PayrollEntity> payrollEntities;

    @Builder.Default
    @OneToMany(mappedBy = "employeeEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<PermissionEntity> permissionEntities = new LinkedHashSet<>();

}
