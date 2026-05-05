package tn.enicarthage.qartnet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.enicarthage.qartnet.model.ProjectPhaseEntity;

@Repository
public interface ProjectPhaseRepository extends JpaRepository<ProjectPhaseEntity, Long> {
}
