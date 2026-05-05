package tn.enicarthage.qartnet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.enicarthage.qartnet.model.ProjectTimelineItemEntity;

@Repository
public interface ProjectTimelineItemJpaRepository extends JpaRepository<ProjectTimelineItemEntity, Long> {
}
