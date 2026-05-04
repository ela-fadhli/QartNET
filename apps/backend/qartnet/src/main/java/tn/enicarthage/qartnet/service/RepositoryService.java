package tn.enicarthage.qartnet.service;

import tn.enicarthage.qartnet.dto.request.RepositoryUpdateRequest;
import tn.enicarthage.qartnet.model.RepositoryEntity;
import tn.enicarthage.qartnet.repository.RepositoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.enicarthage.qartnet.shared.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RepositoryService {

    private final RepositoryJpaRepository repositoryJpaRepository;
    private final GitStorageService gitStorageService;
    private final RepositoryAccessService repositoryAccessService;

    @org.springframework.beans.factory.annotation.Value("${server.port:8080}")
    private String serverPort;

    public List<RepositoryEntity> getAllRepositories() {
        return repositoryJpaRepository.findAll();
    }

    public Optional<RepositoryEntity> getByOwnerAndName(String owner, String name) {
        return repositoryJpaRepository.findByOwnerIgnoreCaseAndNameIgnoreCase(owner, name);
    }

    public RepositoryEntity save(RepositoryEntity repository) {
        // Initialize physical git repository
        String owner = repository.getOwner().trim();
        String name = repository.getName().trim();
        repository.setOwner(owner);
        repository.setName(name);
        
        String repoPath = owner + "/" + name;
        gitStorageService.initBareRepository(repoPath);

        if (repository.getStars() == null) repository.setStars(0);
        if (repository.getForks() == null) repository.setForks(0);
        if (repository.getWatchers() == null) repository.setWatchers(0);
        if (repository.getUpdatedAt() == null) repository.setUpdatedAt("Just now");
        if (repository.getCloneUrl() == null) {
            repository.setCloneUrl("http://localhost:" + serverPort + "/git/" + repository.getOwner() + "/" + repository.getName() + ".git");
        }

        if (repository.getBranches() != null) {
            repository.getBranches().forEach(branch -> {
                branch.setRepository(repository);
            });
        }

        if (repository.getCommits() != null) {
            repository.getCommits().forEach(commit -> {
                commit.setRepository(repository);
            });
        }

        if (repository.getContributors() != null) {
            repository.getContributors().forEach(contributor -> {
                contributor.setRepository(repository);
            });
        }

        if (repository.getFiles() != null) {
            repository.getFiles().forEach(file -> {
                file.setRepository(repository);
            });
        }

        if (repository.getMetrics() != null) {
            repository.getMetrics().setRepository(repository);
        }

        RepositoryEntity saved = repositoryJpaRepository.save(repository);
        repositoryAccessService.bootstrapOwnerAccess(saved);
        return saved;
    }

    public void deleteRepository(Long id) {
        repositoryJpaRepository.findById(id).ifPresent(repo -> {
            String repoPath = repo.getOwner() + "/" + repo.getName();
            gitStorageService.deleteRepository(repoPath);
        });
        repositoryJpaRepository.deleteById(id);
    }

    public RepositoryEntity star(String owner, String name) {
        RepositoryEntity repo = repositoryJpaRepository.findByOwnerIgnoreCaseAndNameIgnoreCase(owner, name)
                .orElseThrow(() -> new RuntimeException("Repository not found"));
        repo.setStars(repo.getStars() + 1);
        return repositoryJpaRepository.save(repo);
    }

    public RepositoryEntity unstar(String owner, String name) {
        RepositoryEntity repo = repositoryJpaRepository.findByOwnerIgnoreCaseAndNameIgnoreCase(owner, name)
                .orElseThrow(() -> new RuntimeException("Repository not found"));
        repo.setStars(Math.max(0, repo.getStars() - 1));
        return repositoryJpaRepository.save(repo);
    }

    public RepositoryEntity fork(String owner, String name, String newOwner) {
        RepositoryEntity original = repositoryJpaRepository.findByOwnerIgnoreCaseAndNameIgnoreCase(owner, name)
                .orElseThrow(() -> new RuntimeException("Repository not found"));

        RepositoryEntity fork = new RepositoryEntity();
        fork.setOwner(newOwner);
        fork.setOwnerDisplayName(newOwner); // Should be fetched from profile in a real app
        fork.setName(original.getName());
        fork.setDescription("Forked from " + owner + "/" + name + ": " + original.getDescription());
        fork.setVisibility(original.getVisibility());
        fork.setLanguage(original.getLanguage());
        
        // Physical clone
        String sourcePath = owner + "/" + name;
        String destPath = newOwner + "/" + name;
        gitStorageService.forkRepository(sourcePath, destPath);

        return save(fork);
    }

    public RepositoryEntity updateRepository(String owner, String name, RepositoryUpdateRequest request) {
        RepositoryEntity repository = repositoryJpaRepository.findByOwnerIgnoreCaseAndNameIgnoreCase(owner, name)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found"));

        String originalOwner = repository.getOwner();
        String originalName = repository.getName();
        String updatedName = request.getName() != null ? request.getName().trim() : repository.getName();

        if (!updatedName.equalsIgnoreCase(originalName)) {
            String sourcePath = originalOwner + "/" + originalName;
            String destinationPath = originalOwner + "/" + updatedName;
            gitStorageService.renameRepository(sourcePath, destinationPath);
            repository.setName(updatedName);
        }

        if (request.getDescription() != null) {
            repository.setDescription(request.getDescription());
        }
        if (request.getVisibility() != null) {
            repository.setVisibility(request.getVisibility());
        }

        repository.setCloneUrl("http://localhost:" + serverPort + "/git/" + repository.getOwner() + "/" + repository.getName() + ".git");
        return repositoryJpaRepository.save(repository);
    }
}
