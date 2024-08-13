package account.database.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "events")
public class CommonEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "created_timestamp", nullable = false)
    private LocalDateTime created;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "object", nullable = false)
    private String object;

    @Column(name = "path", nullable = false)
    private String path;

}
