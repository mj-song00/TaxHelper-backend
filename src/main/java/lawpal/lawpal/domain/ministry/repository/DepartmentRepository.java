package lawpal.lawpal.domain.ministry.repository;

import lawpal.lawpal.domain.ministry.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
