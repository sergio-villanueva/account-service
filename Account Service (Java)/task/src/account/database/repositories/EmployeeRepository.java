package account.database.repositories;

import account.database.entities.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<EmployeeEntity> findByEmailIgnoreCase(String email);
}