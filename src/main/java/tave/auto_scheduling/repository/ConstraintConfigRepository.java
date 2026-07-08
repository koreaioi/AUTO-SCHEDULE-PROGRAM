package tave.auto_scheduling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tave.auto_scheduling.domain.ConstraintConfig;

import java.util.Optional;

public interface ConstraintConfigRepository extends JpaRepository<ConstraintConfig, Long> {
    Optional<ConstraintConfig> findByConstraintName(String constraintName);
}
