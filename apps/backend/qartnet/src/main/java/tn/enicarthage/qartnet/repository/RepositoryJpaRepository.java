package tn.enicarthage.qartnet.repository;

import com.qarnet.qartnetapi.model.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepositoryJpaRepository extends JpaRepository<RepositoryEntity, Long> {
    Optional<RepositoryEntity> findByOwnerAndName(String owner, String name);
}