package tn.enicarthage.qartnet.controller;

import tn.enicarthage.qartnet.dto.request.RepositoryAccessRequest;
import tn.enicarthage.qartnet.dto.request.RepositoryUpdateRequest;
import tn.enicarthage.qartnet.model.RepositoryAccessEntity;
import tn.enicarthage.qartnet.model.RepositoryEntity;
import tn.enicarthage.qartnet.service.RepositoryAccessService;
import tn.enicarthage.qartnet.service.RepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repositories")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class RepositoryController {

    private final RepositoryService repositoryService;
    private final RepositoryAccessService repositoryAccessService;

    @GetMapping
    public ResponseEntity<List<RepositoryEntity>> getRepositories() {
        return ResponseEntity.ok(repositoryService.getAllRepositories());
    }

    @GetMapping("/{owner}/{name}")
    public ResponseEntity<RepositoryEntity> getRepositoryByOwnerAndName(
            @PathVariable String owner,
            @PathVariable String name
    ) {
        return repositoryService.getByOwnerAndName(owner, name)
                .filter(repo -> {
                    repositoryAccessService.ensureReadAccess(repo);
                    return true;
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RepositoryEntity> createRepository(@RequestBody RepositoryEntity repository) {
        return ResponseEntity.ok(repositoryService.save(repository));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRepository(@PathVariable Long id) {
        log.info(">>> Request to delete repository ID: {}", id);
        try {
            repositoryService.deleteRepository(id);
            log.info(">>> Repository {} deleted successfully", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error(">>> Error deleting repository {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{owner}/{name}")
    public ResponseEntity<RepositoryEntity> updateRepository(
            @PathVariable String owner,
            @PathVariable String name,
            @RequestBody RepositoryUpdateRequest request
    ) {
        RepositoryEntity repository = repositoryAccessService.getRepositoryOrThrow(owner, name);
        repositoryAccessService.ensureWriteAccess(repository);
        return ResponseEntity.ok(repositoryService.updateRepository(owner, name, request));
    }

    @PostMapping("/{owner}/{name}/star")
    public ResponseEntity<RepositoryEntity> starRepository(@PathVariable String owner, @PathVariable String name) {
        return ResponseEntity.ok(repositoryService.star(owner, name));
    }

    @PostMapping("/{owner}/{name}/unstar")
    public ResponseEntity<RepositoryEntity> unstarRepository(@PathVariable String owner, @PathVariable String name) {
        return ResponseEntity.ok(repositoryService.unstar(owner, name));
    }

    @PostMapping("/{owner}/{name}/fork")
    public ResponseEntity<RepositoryEntity> forkRepository(@PathVariable String owner, @PathVariable String name, @RequestParam String newOwner) {
        RepositoryEntity repository = repositoryAccessService.getRepositoryOrThrow(owner, name);
        repositoryAccessService.ensureReadAccess(repository);
        return ResponseEntity.ok(repositoryService.fork(owner, name, newOwner));
    }

    @GetMapping("/{owner}/{name}/access")
    public ResponseEntity<List<RepositoryAccessEntity>> listAccess(@PathVariable String owner, @PathVariable String name) {
        return ResponseEntity.ok(repositoryAccessService.listAccess(owner, name));
    }

    @PostMapping("/{owner}/{name}/access")
    public ResponseEntity<RepositoryAccessEntity> grantAccess(
            @PathVariable String owner,
            @PathVariable String name,
            @RequestBody RepositoryAccessRequest request
    ) {
        return ResponseEntity.ok(repositoryAccessService.upsertAccess(owner, name, request));
    }

    @PutMapping("/{owner}/{name}/access")
    public ResponseEntity<RepositoryAccessEntity> updateAccess(
            @PathVariable String owner,
            @PathVariable String name,
            @RequestBody RepositoryAccessRequest request
    ) {
        return ResponseEntity.ok(repositoryAccessService.upsertAccess(owner, name, request));
    }

    @DeleteMapping("/{owner}/{name}/access/{actorKey}")
    public ResponseEntity<Void> revokeAccess(
            @PathVariable String owner,
            @PathVariable String name,
            @PathVariable String actorKey
    ) {
        repositoryAccessService.revokeAccess(owner, name, actorKey);
        return ResponseEntity.noContent().build();
    }
}
