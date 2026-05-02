package tn.enicarthage.qartnet.repository;
import tn.enicarthage.qartnet.model.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectJpaRepository extends JpaRepository<ProjectEntity, Long> {
}
