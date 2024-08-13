package account.models.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EventDTO {
    private LocalDateTime date;
    private String eventType;
    private String subject;
    private String object;
    private String path;
}
