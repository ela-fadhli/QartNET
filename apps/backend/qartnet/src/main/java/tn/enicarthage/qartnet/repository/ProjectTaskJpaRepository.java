package tn.enicarthage.qartnet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.enicarthage.qartnet.model.ProjectTaskEntity;

@Repository
public interface ProjectTaskJpaRepository extends JpaRepository<ProjectTaskEntity, Long> {
}
