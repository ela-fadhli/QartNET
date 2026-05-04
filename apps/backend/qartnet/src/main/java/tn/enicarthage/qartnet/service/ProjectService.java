package tn.enicarthage.qartnet.service;

import tn.enicarthage.qartnet.model.ProjectEntity;
import tn.enicarthage.qartnet.model.RepositoryEntity;
import tn.enicarthage.qartnet.repository.ProjectJpaRepository;
import tn.enicarthage.qartnet.repository.RepositoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.enicarthage.qartnet.shared.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectJpaRepository projectJpaRepository;
    private final RepositoryJpaRepository repositoryJpaRepository;

    public List<ProjectEntity> getAllProjects() {
        return projectJpaRepository.findAll();
    }

    public Optional<ProjectEntity> getProjectById(Long id) {
        return projectJpaRepository.findById(id);
    }

    public ProjectEntity save(ProjectEntity project) {
        syncLinkedRepository(project);
        if (project.getProgress() == null) project.setProgress(0);
        if (project.getTeamCount() == null) project.setTeamCount(1);
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

    public ProjectEntity linkRepository(Long projectId, Long repositoryId) {
        ProjectEntity project = projectJpaRepository.findById(projectId)
                .orElseThrow(() -> ResourceNotFoundException.of("Project", projectId));
        RepositoryEntity repository = repositoryJpaRepository.findById(repositoryId)
                .orElseThrow(() -> ResourceNotFoundException.of("Repository", repositoryId));
        project.setRepository(repository);
        project.setRepositoryId(repository.getId());
        project.setRepositoryName(repository.getOwner() + "/" + repository.getName());
        return projectJpaRepository.save(project);
    }

    public ProjectEntity unlinkRepository(Long projectId) {
        ProjectEntity project = projectJpaRepository.findById(projectId)
                .orElseThrow(() -> ResourceNotFoundException.of("Project", projectId));
        project.setRepository(null);
        project.setRepositoryId(null);
        project.setRepositoryName(null);
        return projectJpaRepository.save(project);
    }

    private void syncLinkedRepository(ProjectEntity project) {
        RepositoryEntity repository = null;
        if (project.getRepository() != null && project.getRepository().getId() != null) {
            repository = repositoryJpaRepository.findById(project.getRepository().getId()).orElse(null);
        } else if (project.getRepositoryId() != null) {
            repository = repositoryJpaRepository.findById(project.getRepositoryId()).orElse(null);
        }
        if (repository != null) {
            project.setRepository(repository);
            project.setRepositoryId(repository.getId());
            project.setRepositoryName(repository.getOwner() + "/" + repository.getName());
        }
    }
    public void deleteProject(Long id) {
        projectJpaRepository.deleteById(id);
    }
}
