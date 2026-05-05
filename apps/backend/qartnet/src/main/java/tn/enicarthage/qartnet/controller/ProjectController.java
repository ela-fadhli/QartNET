package tn.enicarthage.qartnet.controller;

import tn.enicarthage.qartnet.model.*;
import tn.enicarthage.qartnet.service.ProjectService;
import tn.enicarthage.qartnet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ProjectController {

    private final ProjectService projectService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<ProjectEntity>> getProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectEntity> getProjectById(@PathVariable Long id, Authentication auth) {
        return projectService.getProjectById(id)
                .map(project -> {
                    String username = resolveUsername(auth);
                    if (username != null) {
                        boolean needsRepair = false;

                        // Case 1: creatorUsername is missing entirely
                        if (project.getCreatorUsername() == null || project.getCreatorUsername().isBlank()) {
                            needsRepair = true;
                        }

                        // Case 2: creatorUsername is set but doesn't match the creator's display name
                        // (legacy data where creatorName was set correctly but creatorUsername was wrong)
                        if (!needsRepair && project.getCreatorName() != null) {
                            String displayName = resolveDisplayName(auth);
                            if (displayName != null && project.getCreatorName().equalsIgnoreCase(displayName)
                                    && !username.equals(project.getCreatorUsername())) {
                                needsRepair = true;
                            }
                        }

                        if (needsRepair) {
                            project.setCreatorUsername(username);
                            projectService.save(project);
                        }
                    }
                    return ResponseEntity.ok(project);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProjectEntity> createProject(@RequestBody ProjectEntity project, Authentication auth) {
        // Always set creatorUsername from the authenticated user (server-side enforcement)
        String username = resolveUsername(auth);
        if (username != null) {
            project.setCreatorUsername(username);
            if (project.getCreatorName() == null || project.getCreatorName().isBlank()) {
                project.setCreatorName(username);
            }
        }
        return ResponseEntity.ok(projectService.save(project));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/repository/{repositoryId}")
    public ResponseEntity<ProjectEntity> linkRepository(@PathVariable Long id, @PathVariable Long repositoryId) {
        return ResponseEntity.ok(projectService.linkRepository(id, repositoryId));
    }

    @DeleteMapping("/{id}/repository")
    public ResponseEntity<ProjectEntity> unlinkRepository(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.unlinkRepository(id));
    }

    @PatchMapping("/tasks/{taskId}/status")
    public ResponseEntity<ProjectEntity> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestParam String status) {
        return ResponseEntity.ok(projectService.updateTaskStatus(taskId, status));
    }

    @GetMapping("/{id}/recalculate-progress")
    public ResponseEntity<ProjectEntity> recalculateProgress(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.calculateAndUpdateProgress(id));
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<?> addMember(@PathVariable Long projectId, @RequestBody ProjectMemberEntity member, Authentication auth) {
        if (!isProjectCreator(projectId, auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the project creator can add team members.");
        }
        return ResponseEntity.ok(projectService.addMember(projectId, member));
    }

    @PostMapping("/{projectId}/phases")
    public ResponseEntity<?> addPhase(@PathVariable Long projectId, @RequestBody ProjectPhaseEntity phase, Authentication auth) {
        if (!isProjectCreator(projectId, auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the project creator can add phases.");
        }
        return ResponseEntity.ok(projectService.addPhase(projectId, phase));
    }

    @PostMapping("/phases/{phaseId}/tasks")
    public ResponseEntity<?> addTask(@PathVariable Long phaseId, @RequestBody ProjectTaskEntity task, Authentication auth) {
        Long projectId = projectService.getProjectIdByPhaseId(phaseId);
        if (projectId == null || !isProjectCreator(projectId, auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the project creator can add tasks.");
        }
        return ResponseEntity.ok(projectService.addTask(phaseId, task));
    }

    @PostMapping("/repair-ownership")
    public ResponseEntity<String> repairOwnership(@RequestParam String username, @RequestParam String name) {
        projectService.repairOwnership(username, name);
        return ResponseEntity.ok("Ownership repaired for all projects.");
    }

    /**
     * Resolves the profile username from the JWT authentication (publicId -> username).
     */
    private String resolveUsername(Authentication auth) {
        if (auth == null) return null;
        try {
            UUID publicId = UUID.fromString(auth.getName());
            return userRepository.findByPublicId(publicId)
                    .map(user -> user.getProfile() != null ? user.getProfile().getUsername() : null)
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Resolves the display name (firstName lastName) from the JWT authentication.
     */
    private String resolveDisplayName(Authentication auth) {
        if (auth == null) return null;
        try {
            UUID publicId = UUID.fromString(auth.getName());
            return userRepository.findByPublicId(publicId)
                    .map(user -> {
                        String first = user.getFirstName();
                        String last = user.getLastName();
                        if (first != null && last != null) return first + " " + last;
                        if (user.getProfile() != null) return user.getProfile().getUsername();
                        return null;
                    })
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if the authenticated user is the creator of the given project.
     */
    private boolean isProjectCreator(Long projectId, Authentication auth) {
        String username = resolveUsername(auth);
        if (username == null) return false;

        return projectService.getProjectById(projectId)
                .map(project -> username.equals(project.getCreatorUsername()))
                .orElse(false);
    }
}
