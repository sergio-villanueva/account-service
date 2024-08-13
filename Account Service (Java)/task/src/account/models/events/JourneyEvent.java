package account.models.events;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter @Setter
public class JourneyEvent {
    private LocalDateTime date;
    private String eventType;
    private String subject;
    private String object;
    private String path;

    public JourneyEvent(LocalDateTime date, String eventType, String subject, String object, String path) {
        this.date = date;
        this.eventType = eventType;
        this.subject = subject;
        this.object = object;
        this.path = path;
    }
}
