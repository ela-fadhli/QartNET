package tn.enicarthage.qartnet.repository;

import tn.enicarthage.qartnet.model.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepositoryJpaRepository extends JpaRepository<RepositoryEntity, Long> {
    Optional<RepositoryEntity> findByOwnerIgnoreCaseAndNameIgnoreCase(String owner, String name);
}
