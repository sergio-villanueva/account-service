package account.database.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
//@Table(name = "BREACHED_PASSWORDS")
@Table(name = "breached_passwords")
public class BreachedPasswordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "password", nullable = false)
    private String password;

}
