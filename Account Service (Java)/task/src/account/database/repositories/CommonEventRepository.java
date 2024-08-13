package account.database.repositories;

import account.database.entities.CommonEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommonEventRepository extends JpaRepository<CommonEventEntity, Long> {

}