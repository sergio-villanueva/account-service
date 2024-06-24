package account.database.repositories;

import account.database.entities.PayrollEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<PayrollEntity, Long> {

    @Query("select p from PayrollEntity p where lower(p.employeeEntity.email) = lower(?1)")
    Optional<PayrollEntity> findByEmailgnoreCase(String email);
}