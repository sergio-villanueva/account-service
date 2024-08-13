package account.listeners;

import account.database.entities.BreachedPasswordEntity;
import account.database.repositories.BreachedPasswordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class LoadTestDataListener implements ApplicationListener<ApplicationReadyEvent> {

    private final BreachedPasswordRepository breachedPasswordRepository;

    @Autowired
    public LoadTestDataListener(BreachedPasswordRepository breachedPasswordRepository) {
        this.breachedPasswordRepository = breachedPasswordRepository;
    }

    /**
     * The listener used to load test data by waiting for application startup
     * @param event the event indicating when the application started up and ready
     * */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // initialize test data here
        List<BreachedPasswordEntity> testData = List.of(
                new BreachedPasswordEntity(1L, "PasswordForJanuary"),
                new BreachedPasswordEntity(2L, "PasswordForFebruary"),
                new BreachedPasswordEntity(3L, "PasswordForMarch"),
                new BreachedPasswordEntity(4L, "PasswordForApril"),
                new BreachedPasswordEntity(5L, "PasswordForMay"),
                new BreachedPasswordEntity(6L, "PasswordForJune"),
                new BreachedPasswordEntity(7L, "PasswordForJuly"),
                new BreachedPasswordEntity(8L, "PasswordForAugust"),
                new BreachedPasswordEntity(9L, "PasswordForSeptember"),
                new BreachedPasswordEntity(10L, "PasswordForOctober"),
                new BreachedPasswordEntity(11L, "PasswordForNovember"),
                new BreachedPasswordEntity(12L, "PasswordForDecember")
        );

        breachedPasswordRepository.saveAll(testData);
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
