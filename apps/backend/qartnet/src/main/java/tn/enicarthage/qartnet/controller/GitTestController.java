package tn.enicarthage.qartnet.controller;

import tn.enicarthage.qartnet.service.GitStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/git")
public class GitTestController {

    private final GitStorageService gitStorageService;

    public GitTestController(GitStorageService gitStorageService) {
        this.gitStorageService = gitStorageService;
    }

    @PostMapping("/init")
    public ResponseEntity<String> initRepository(@RequestParam String repoName) {
        boolean success = gitStorageService.initBareRepository(repoName);
        if (success) {
            return ResponseEntity.ok("Repository " + repoName + " initialized successfully at " + gitStorageService.getBasePath());
        } else {
            return ResponseEntity.internalServerError().body("Failed to initialize repository " + repoName);
        }
    }
}
