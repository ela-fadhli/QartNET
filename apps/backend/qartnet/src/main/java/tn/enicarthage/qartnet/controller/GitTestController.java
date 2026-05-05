package tn.enicarthage.qartnet.controller;

import tn.enicarthage.qartnet.service.GitStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/git")
@RequiredArgsConstructor
public class GitTestController {

    private final GitStorageService gitStorageService;

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
