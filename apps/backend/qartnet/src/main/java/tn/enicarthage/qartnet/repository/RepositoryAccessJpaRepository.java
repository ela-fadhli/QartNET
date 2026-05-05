package tn.enicarthage.qartnet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.enicarthage.qartnet.model.RepositoryAccessEntity;

import java.util.List;
import java.util.Optional;

public interface RepositoryAccessJpaRepository extends JpaRepository<RepositoryAccessEntity, Long> {
    Optional<RepositoryAccessEntity> findByRepositoryIdAndActorKeyIgnoreCase(Long repositoryId, String actorKey);
    List<RepositoryAccessEntity> findByRepositoryId(Long repositoryId);
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByRepositoryIdAndActorKeyIgnoreCase(Long repositoryId, String actorKey);
}
