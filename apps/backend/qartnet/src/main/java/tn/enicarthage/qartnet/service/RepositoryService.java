package tn.enicarthage.qartnet.service;

import com.qarnet.qartnetapi.model.RepositoryEntity;
import com.qarnet.qartnetapi.repository.RepositoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RepositoryService {

    private final RepositoryJpaRepository repositoryJpaRepository;

    public List<RepositoryEntity> getAllRepositories() {
        return repositoryJpaRepository.findAll();
    }

    public Optional<RepositoryEntity> getByOwnerAndName(String owner, String name) {
        return repositoryJpaRepository.findByOwnerAndName(owner, name);
    }

    public RepositoryEntity save(RepositoryEntity repository) {

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

        return repositoryJpaRepository.save(repository);
    }
}