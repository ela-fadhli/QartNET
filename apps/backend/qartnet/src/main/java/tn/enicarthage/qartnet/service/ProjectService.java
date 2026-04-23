package tn.enicarthage.qartnet.service;

import com.qarnet.qartnetapi.model.ProjectEntity;
import com.qarnet.qartnetapi.repository.ProjectJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectJpaRepository projectJpaRepository;

    public List<ProjectEntity> getAllProjects() {
        return projectJpaRepository.findAll();
    }

    public Optional<ProjectEntity> getProjectById(Long id) {
        return projectJpaRepository.findById(id);
    }

    public ProjectEntity save(ProjectEntity project) {
        if (project.getPhases() != null) {
            project.getPhases().forEach(phase -> {
                phase.setProject(project);

                if (phase.getTasks() != null) {
                    phase.getTasks().forEach(task -> task.setPhase(phase));
                }
            });
        }

        if (project.getTeam() != null) {
            project.getTeam().forEach(member -> member.setProject(project));
        }

        if (project.getTimeline() != null) {
            project.getTimeline().forEach(item -> item.setProject(project));
        }

        return projectJpaRepository.save(project);
    }
}