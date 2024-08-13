package account.database.repositories;

import account.database.entities.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<EmployeeEntity> findByEmailIgnoreCase(String email);

    @Transactional
    @Modifying
    @Query("update EmployeeEntity e set e.lockFlag = ?1 where upper(e.email) = upper(?2)")
    void updateLockFlagByEmailIgnoreCase(Boolean lockFlag, String email);

}