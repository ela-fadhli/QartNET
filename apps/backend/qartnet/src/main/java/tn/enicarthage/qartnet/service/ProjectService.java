package tn.enicarthage.qartnet.service;

import tn.enicarthage.qartnet.model.*;
import tn.enicarthage.qartnet.repository.ProjectJpaRepository;
import tn.enicarthage.qartnet.repository.RepositoryJpaRepository;
import tn.enicarthage.qartnet.repository.ProjectTaskJpaRepository;
import tn.enicarthage.qartnet.repository.ProjectTimelineItemJpaRepository;
import tn.enicarthage.qartnet.repository.ProjectPhaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.enicarthage.qartnet.shared.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectJpaRepository projectJpaRepository;
    private final RepositoryJpaRepository repositoryJpaRepository;
    private final ProjectTaskJpaRepository taskJpaRepository;
    private final ProjectTimelineItemJpaRepository timelineJpaRepository;
    private final ProjectPhaseRepository phaseJpaRepository;

    public List<ProjectEntity> getAllProjects() {
        return projectJpaRepository.findAll();
    }

    public Optional<ProjectEntity> getProjectById(Long id) {
        Optional<ProjectEntity> projectOpt = projectJpaRepository.findById(id);
        if (projectOpt.isPresent()) {
            ProjectEntity project = projectOpt.get();
            if (project.getTeam() == null || project.getTeam().isEmpty()) {
                String leadName = project.getCreatorName() != null ? project.getCreatorName() : "Project Lead";
                ProjectMemberEntity lead = ProjectMemberEntity.builder()
                        .name(leadName)
                        .role("Lead Engineer")
                        .specialty("System Architecture")
                        .initials("PL")
                        .project(project)
                        .avatarUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=Lead")
                        .build();
                project.getTeam().add(lead);
                projectJpaRepository.save(project);
            }
            if (project.getTimeline() == null || project.getTimeline().isEmpty()) {
                ProjectTimelineItemEntity initialEvent = ProjectTimelineItemEntity.builder()
                        .title("Project Initialized")
                        .date(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                        .icon("pi pi-plus")
                        .color("#C9A84C")
                        .project(project)
                        .build();
                timelineJpaRepository.save(initialEvent);
            }

            if (project.getPhases() == null || project.getPhases().isEmpty()) {
                initializeDefaultPhases(project);
            }

            return Optional.of(calculateAndUpdateProgress(id));
        }
        return projectOpt;
    }

    public ProjectEntity save(ProjectEntity project) {
        boolean isNew = project.getId() == null;
        syncLinkedRepository(project);
        
        if (project.getProgress() == null) {
            project.setProgress(0);
        }
        if (project.getTeamCount() == null) {
            project.setTeamCount(1);
        }
        
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

        ProjectEntity savedProject = projectJpaRepository.save(project);

        // Add initial timeline item and creator if new project and team is empty
        if (isNew && (project.getTeam() == null || project.getTeam().isEmpty())) {
            // Add creator to team
            String leadName = project.getCreatorName() != null ? project.getCreatorName() : "Project Lead";
            String leadUsername = project.getCreatorUsername() != null ? project.getCreatorUsername() : "admin";
            
            ProjectMemberEntity lead = ProjectMemberEntity.builder()
                    .name(leadName)
                    .role("Lead Engineer")
                    .specialty("System Architecture")
                    .userId(leadUsername)
                    .avatarUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=" + leadName)
                    .project(project)
                    .build();
            project.getTeam().add(lead);
            projectJpaRepository.save(savedProject);
        }

        if (isNew) {
            ProjectTimelineItemEntity initialEvent = ProjectTimelineItemEntity.builder()
                    .title("Project Initialized")
                    .date(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .icon("pi pi-plus")
                    .color("#C9A84C")
                    .project(savedProject)
                    .build();
            timelineJpaRepository.save(initialEvent);
        }

        return savedProject;
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

    public ProjectEntity updateTaskStatus(Long taskId, String status) {
        ProjectTaskEntity task = taskJpaRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        
        String oldStatus = task.getStatus();
        task.setStatus(status);
        taskJpaRepository.save(task);

        ProjectEntity project = task.getPhase().getProject();
        
        // If task completed, add to timeline
        if ("completed".equalsIgnoreCase(status) && !"completed".equalsIgnoreCase(oldStatus)) {
            ProjectTimelineItemEntity item = ProjectTimelineItemEntity.builder()
                    .title("Task Completed: " + task.getTitle())
                    .date(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .icon("pi pi-check")
                    .color("#4CAF50")
                    .project(project)
                    .build();
            timelineJpaRepository.save(item);
        }

        return calculateAndUpdateProgress(project.getId());
    }

    public ProjectEntity calculateAndUpdateProgress(Long projectId) {
        ProjectEntity project = projectJpaRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        List<ProjectPhaseEntity> phases = project.getPhases();
        if (phases == null || phases.isEmpty()) {
            project.setProgress(0);
            return projectJpaRepository.save(project);
        }

        long totalTasks = 0;
        long completedTasks = 0;

        for (ProjectPhaseEntity phase : phases) {
            if (phase.getTasks() != null && !phase.getTasks().isEmpty()) {
                long phaseTotal = phase.getTasks().size();
                long phaseCompleted = phase.getTasks().stream()
                        .filter(t -> "completed".equalsIgnoreCase(t.getStatus()))
                        .count();
                
                totalTasks += phaseTotal;
                completedTasks += phaseCompleted;

                // Update individual phase status
                if (phaseCompleted == phaseTotal) {
                    phase.setStatus("COMPLETED");
                } else if (phaseCompleted > 0) {
                    phase.setStatus("IN_PROGRESS");
                } else {
                    phase.setStatus("PENDING");
                }
                
                int phaseProgress = (int) ((phaseCompleted * 100) / phaseTotal);
                phase.setProgress(phaseProgress);
            } else {
                phase.setStatus("PENDING");
                phase.setProgress(0);
            }
        }

        int progress = totalTasks > 0 ? (int) ((completedTasks * 100) / totalTasks) : 0;
        project.setProgress(progress);
        
        return projectJpaRepository.save(project);
    }

    public ProjectEntity addMember(Long projectId, ProjectMemberEntity member) {
        ProjectEntity project = projectJpaRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        member.setProject(project);
        project.getTeam().add(member);
        
        ProjectTimelineItemEntity item = ProjectTimelineItemEntity.builder()
                .title("New Team Member: " + member.getName())
                .date(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .icon("pi pi-user-plus")
                .color("#C9A84C")
                .project(project)
                .build();
        timelineJpaRepository.save(item);
        
        return projectJpaRepository.save(project);
    }

    public ProjectEntity addPhase(Long projectId, ProjectPhaseEntity phase) {
        ProjectEntity project = projectJpaRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        phase.setProject(project);
        project.getPhases().add(phase);
        
        ProjectTimelineItemEntity item = ProjectTimelineItemEntity.builder()
                .title("New Phase Added: " + phase.getTitle())
                .date(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .icon("pi pi-map")
                .color("#C9A84C")
                .project(project)
                .build();
        timelineJpaRepository.save(item);
        
        return projectJpaRepository.save(project);
    }

    public ProjectEntity addTask(Long phaseId, ProjectTaskEntity task) {
        ProjectPhaseEntity phase = phaseJpaRepository.findById(phaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Phase not found"));
        
        task.setPhase(phase);
        taskJpaRepository.save(task);
        
        ProjectTimelineItemEntity item = ProjectTimelineItemEntity.builder()
                .title("New Task Dispatched: " + task.getTitle())
                .date(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .icon("pi pi-send")
                .color("#C9A84C")
                .project(phase.getProject())
                .build();
        timelineJpaRepository.save(item);
        
        return calculateAndUpdateProgress(phase.getProject().getId());
    }

    public void deleteProject(Long id) {
        projectJpaRepository.deleteById(id);
    }

    public Long getProjectIdByPhaseId(Long phaseId) {
        return phaseJpaRepository.findById(phaseId)
                .map(phase -> phase.getProject().getId())
                .orElse(null);
    }

    public void repairOwnership(String username, String name) {
        List<ProjectEntity> projects = projectJpaRepository.findAll();
        for (ProjectEntity project : projects) {
            if (project.getCreatorUsername() == null) {
                project.setCreatorUsername(username);
                project.setCreatorName(name);
                projectJpaRepository.save(project);
            }
        }
    }

    private void initializeDefaultPhases(ProjectEntity project) {
        String[] titles = {
            "Project Concept & Research",
            "System Architecture & Design",
            "Core Development & Implementation",
            "Quality Assurance & Testing",
            "Final Deployment & Delivery"
        };
        
        for (String title : titles) {
            ProjectPhaseEntity phase = ProjectPhaseEntity.builder()
                    .title(title)
                    .status("PENDING")
                    .progress(0)
                    .project(project)
                    .build();
            project.getPhases().add(phase);
        }
        projectJpaRepository.save(project);
    }
}
