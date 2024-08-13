package account.listeners;

import account.database.entities.CommonEventEntity;
import account.database.repositories.CommonEventRepository;
import account.models.events.JourneyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class JourneyEventListener {

    private final static Logger logger = LoggerFactory.getLogger(JourneyEventListener.class);

    private final CommonEventRepository commonEventRepository;

    @Autowired
    public JourneyEventListener(CommonEventRepository commonEventRepository) {
        this.commonEventRepository = commonEventRepository;
    }

    /** This listener is used to handle business journey events
     * @param journeyEvent the journey event to handle
     * */
    @Async("listenerExecutor")
    @EventListener
    public void onJourneyEvent(JourneyEvent journeyEvent) {
        logger.info(String.format("handling event for:\nsubject: %s\nevent: %s\nobject: %s\npath: %s",
                journeyEvent.getSubject(),
                journeyEvent.getEventType(),
                journeyEvent.getObject(),
                journeyEvent.getPath()));
        // convert journey event to entity and save in db
        commonEventRepository.save(toCommonEventEntity(journeyEvent));
    }

    private CommonEventEntity toCommonEventEntity(JourneyEvent journeyEvent) {
        return CommonEventEntity.builder()
                .created(journeyEvent.getDate())
                .eventType(journeyEvent.getEventType())
                .subject(journeyEvent.getSubject())
                .object(journeyEvent.getObject())
                .path(journeyEvent.getPath())
                .build();
    }

}
