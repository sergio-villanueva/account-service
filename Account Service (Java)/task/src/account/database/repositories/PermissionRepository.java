package account.database.repositories;

import account.database.entities.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {

    @Query("select p from PermissionEntity p where upper(p.employeeEntity.email) = upper(?1)")
    List<PermissionEntity> findByEmailIgnoreCase(String email);
}