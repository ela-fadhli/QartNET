package tn.enicarthage.qartnet.controller;

import tn.enicarthage.qartnet.model.RepositoryEntity;
import tn.enicarthage.qartnet.service.RepositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repositories")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class RepositoryController {

    private final RepositoryService repositoryService;

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
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RepositoryEntity> createRepository(@RequestBody RepositoryEntity repository) {
        return ResponseEntity.ok(repositoryService.save(repository));
    }
}
