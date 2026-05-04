package tn.enicarthage.qartnet.controller;

import tn.enicarthage.qartnet.model.ProjectEntity;
import tn.enicarthage.qartnet.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectEntity>> getProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectEntity> getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProjectEntity> createProject(@RequestBody ProjectEntity project) {
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
}
