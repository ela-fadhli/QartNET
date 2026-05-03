package tn.enicarthage.qartnet.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class GitStorageService {

    @Value("${qartnet.git.storage-path:git-storage}")
    private String storagePath;

    private Path basePath;

    @PostConstruct
    public void init() throws IOException {
        basePath = Paths.get(storagePath).toAbsolutePath().normalize();
        if (!Files.exists(basePath)) {
            Files.createDirectories(basePath);
        }
    }

    public Path getBasePath() {
        return basePath;
    }

    /**
     * Initializes a bare git repository on the disk.
     * 
     * @param repositoryName e.g. "username/project.git"
     * @return true if initialized successfully or already exists
     */
    public boolean initBareRepository(String repositoryName) {
        if (!repositoryName.endsWith(".git")) {
            repositoryName += ".git";
        }
        File repoDir = basePath.resolve(repositoryName).toFile();
        
        if (repoDir.exists() && repoDir.isDirectory()) {
            // Already exists, consider it a success
            return true;
        }

        try (Git git = Git.init().setDirectory(repoDir).setBare(true).call()) {
            return true;
        } catch (GitAPIException e) {
            e.printStackTrace();
            return false;
        }
    }
}
