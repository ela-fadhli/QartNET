package tn.enicarthage.qartnet.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.qartnet.dto.request.BranchCreateRequest;
import tn.enicarthage.qartnet.dto.request.BranchMergeRequest;
import tn.enicarthage.qartnet.model.RepositoryCommitEntity;
import tn.enicarthage.qartnet.model.RepositoryFileEntity;
import tn.enicarthage.qartnet.service.GitAnalysisService;
import tn.enicarthage.qartnet.service.RepositoryAccessService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/repositories/{owner}/{name}")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class GitContentController {

    private final GitAnalysisService gitAnalysisService;
    private final RepositoryAccessService repositoryAccessService;

    @GetMapping("/branches")
    public ResponseEntity<List<String>> getBranches(@PathVariable String owner, @PathVariable String name) {
        repositoryAccessService.ensureReadAccess(repositoryAccessService.getRepositoryOrThrow(owner, name));
        return ResponseEntity.ok(gitAnalysisService.getBranches(owner, name));
    }

    @GetMapping("/files")
    public ResponseEntity<List<RepositoryFileEntity>> getFiles(
            @PathVariable String owner, 
            @PathVariable String name, 
            @RequestParam(defaultValue = "main") String branch,
            @RequestParam(required = false) String path) {
        repositoryAccessService.ensureReadAccess(repositoryAccessService.getRepositoryOrThrow(owner, name));
        return ResponseEntity.ok(gitAnalysisService.getFiles(owner, name, branch, path));
    }

    @GetMapping("/commits")
    public ResponseEntity<List<RepositoryCommitEntity>> getCommits(
            @PathVariable String owner, 
            @PathVariable String name, 
            @RequestParam(defaultValue = "main") String branch) {
        repositoryAccessService.ensureReadAccess(repositoryAccessService.getRepositoryOrThrow(owner, name));
        return ResponseEntity.ok(gitAnalysisService.getCommits(owner, name, branch));
    }

    @GetMapping("/commits/{hash}")
    public ResponseEntity<RepositoryCommitEntity> getCommitDetails(
            @PathVariable String owner,
            @PathVariable String name,
            @PathVariable String hash) {
        repositoryAccessService.ensureReadAccess(repositoryAccessService.getRepositoryOrThrow(owner, name));
        RepositoryCommitEntity details = gitAnalysisService.getCommitDetails(owner, name, hash);
        return details != null ? ResponseEntity.ok(details) : ResponseEntity.notFound().build();
    }

    @GetMapping("/content")
    public ResponseEntity<Map<String, String>> getFileContent(
            @PathVariable String owner, 
            @PathVariable String name, 
            @RequestParam String path,
            @RequestParam(defaultValue = "main") String branch) {
        repositoryAccessService.ensureReadAccess(repositoryAccessService.getRepositoryOrThrow(owner, name));
        String content = gitAnalysisService.getFileContent(owner, name, branch, path);
        if (content != null) {
            return ResponseEntity.ok(Map.of("content", content));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/download")
    public void downloadZip(
            @PathVariable String owner, 
            @PathVariable String name, 
            @RequestParam(defaultValue = "main") String branch,
            HttpServletResponse response) throws Exception {
        repositoryAccessService.ensureReadAccess(repositoryAccessService.getRepositoryOrThrow(owner, name));
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" + name + "-" + branch + ".zip");
        gitAnalysisService.streamZip(owner, name, branch, response.getOutputStream());
    }

    @PostMapping("/branches")
    public ResponseEntity<Map<String, String>> createBranch(
            @PathVariable String owner,
            @PathVariable String name,
            @RequestBody BranchCreateRequest request
    ) {
        repositoryAccessService.ensureWriteAccess(repositoryAccessService.getRepositoryOrThrow(owner, name));
        String message = gitAnalysisService.createBranch(owner, name, request.getName(), request.getFromBranch());
        return ResponseEntity.ok(Map.of("message", message));
    }

    @DeleteMapping("/branches/{branchName}")
    public ResponseEntity<Void> deleteBranch(
            @PathVariable String owner,
            @PathVariable String name,
            @PathVariable String branchName
    ) {
        repositoryAccessService.ensureWriteAccess(repositoryAccessService.getRepositoryOrThrow(owner, name));
        gitAnalysisService.deleteBranch(owner, name, branchName);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/merge")
    public ResponseEntity<Map<String, String>> mergeBranch(
            @PathVariable String owner,
            @PathVariable String name,
            @RequestBody BranchMergeRequest request
    ) {
        repositoryAccessService.ensureWriteAccess(repositoryAccessService.getRepositoryOrThrow(owner, name));
        String message = gitAnalysisService.mergeBranch(owner, name, request.getSourceBranch(), request.getTargetBranch());
        return ResponseEntity.ok(Map.of("message", message));
    }
}
