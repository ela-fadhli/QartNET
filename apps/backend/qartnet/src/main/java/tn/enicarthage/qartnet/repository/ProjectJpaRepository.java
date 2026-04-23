package tn.enicarthage.qartnet.repository;
import com.qarnet.qartnetapi.model.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectJpaRepository extends JpaRepository<ProjectEntity, Long> {
}