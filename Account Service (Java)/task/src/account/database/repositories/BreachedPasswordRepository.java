package account.database.repositories;

import account.database.entities.BreachedPasswordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface BreachedPasswordRepository extends JpaRepository<BreachedPasswordEntity, Long> {

    boolean existsByPassword(@NonNull String password);
}