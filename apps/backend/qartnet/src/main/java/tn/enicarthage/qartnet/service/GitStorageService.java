package tn.enicarthage.qartnet.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
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

        // Ensure parent directories exist (e.g. the owner's directory)
        if (repoDir.getParentFile() != null && !repoDir.getParentFile().exists()) {
            repoDir.getParentFile().mkdirs();
        }

        log.info("Attempting to initialize bare repository at: {}", repoDir.getAbsolutePath());

        try (Git _ = Git.init().setDirectory(repoDir).setBare(true).call()) {
            log.info("Successfully initialized bare repository at {}", repoDir.getAbsolutePath());
            return true;
        } catch (GitAPIException e) {
            log.error("Failed to initialize bare repository at {}: {}", repoDir.getAbsolutePath(), e.getMessage(), e);
            return false;
        }

    }

    /**
     * Deletes the physical git repository from the disk.
     *
     * @param repositoryName e.g. "username/project.git"
     * @return true if deleted successfully or doesn't exist
     */
    public boolean deleteRepository(String repositoryName) {
        if (!repositoryName.endsWith(".git")) {
            repositoryName += ".git";
        }
        File repoDir = basePath.resolve(repositoryName).toFile();

        if (!repoDir.exists()) {
            return true;
        }

        log.info("Attempting to delete physical repository at: {}", repoDir.getAbsolutePath());
        return deleteDirectory(repoDir);
    }

    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public void forkRepository(String sourceName, String destName) {
        if (!sourceName.endsWith(".git")) sourceName += ".git";
        if (!destName.endsWith(".git")) destName += ".git";

        File sourceDir = basePath.resolve(sourceName).toFile();
        File destDir = basePath.resolve(destName).toFile();

        if (destDir.exists()) {
            log.warn("Fork destination already exists: {}", destDir.getAbsolutePath());
            return;
        }

        // Ensure parent directories exist
        if (destDir.getParentFile() != null && !destDir.getParentFile().exists()) {
            destDir.getParentFile().mkdirs();
        }

        try (Git _ = Git.cloneRepository()
                .setURI(sourceDir.getAbsolutePath())
                .setDirectory(destDir)
                .setBare(true)
                .call()) {
            log.info("Successfully forked repository from {} to {}", sourceDir.getAbsolutePath(), destDir.getAbsolutePath());
        } catch (GitAPIException e) {
            log.error("Failed to fork repository: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fork repository", e);
        }
    }

    public void renameRepository(String sourceName, String destinationName) {
        if (!sourceName.endsWith(".git")) sourceName += ".git";
        if (!destinationName.endsWith(".git")) destinationName += ".git";

        Path sourcePath = basePath.resolve(sourceName).normalize();
        Path destinationPath = basePath.resolve(destinationName).normalize();

        if (!Files.exists(sourcePath)) {
            throw new IllegalArgumentException("Source repository does not exist: " + sourceName);
        }
        if (Files.exists(destinationPath)) {
            throw new IllegalStateException("Destination repository already exists: " + destinationName);
        }

        try {
            Path parent = destinationPath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            try {
                Files.move(sourcePath, destinationPath, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(sourcePath, destinationPath);
            }
            log.info("Successfully renamed repository from {} to {}", sourceName, destinationName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to rename repository on disk", e);
        }
    }
}
