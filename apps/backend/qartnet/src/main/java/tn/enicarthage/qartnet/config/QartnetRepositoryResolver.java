package tn.enicarthage.qartnet.config;

import tn.enicarthage.qartnet.service.GitStorageService;
import tn.enicarthage.qartnet.service.RepositoryAccessService;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

@Component
public class QartnetRepositoryResolver implements RepositoryResolver<HttpServletRequest> {

    private final GitStorageService gitStorageService;
    private final RepositoryAccessService repositoryAccessService;

    public QartnetRepositoryResolver(GitStorageService gitStorageService, RepositoryAccessService repositoryAccessService) {
        this.gitStorageService = gitStorageService;
        this.repositoryAccessService = repositoryAccessService;
    }

    @Override
    public Repository open(HttpServletRequest request, String name) 
            throws RepositoryNotFoundException, ServiceNotAuthorizedException, ServiceNotEnabledException {
        
        // "name" correspond au chemin après /git/ (ex: username/project.git)
        String repoPath = name;
        if (!repoPath.endsWith(".git")) {
            repoPath += ".git";
        }
        
        String[] segments = repoPath.replace(".git", "").split("/");
        if (segments.length >= 2) {
            String owner = segments[0];
            String repositoryName = segments[1];
            var repositoryEntity = repositoryAccessService.getRepositoryOrThrow(owner, repositoryName);
            boolean writeOperation = isWriteOperation(request);
            if (writeOperation) {
                if (!repositoryAccessService.hasWriteAccess(repositoryEntity)) {
                    throw new ServiceNotAuthorizedException();
                }
            } else if (repositoryEntity.getVisibility() != tn.enicarthage.qartnet.model.RepositoryVisibility.PUBLIC
                    && !repositoryAccessService.hasReadAccess(repositoryEntity)) {
                throw new ServiceNotAuthorizedException();
            }
        }

        File repoDir = gitStorageService.getBasePath().resolve(repoPath).toFile();
        
        if (!repoDir.exists() || !repoDir.isDirectory()) {
            throw new RepositoryNotFoundException(name);
        }

        try {
            return new FileRepository(repoDir);
        } catch (IOException e) {
            throw new RepositoryNotFoundException(name, e);
        }
    }

    private boolean isWriteOperation(HttpServletRequest request) {
        String uri = request.getRequestURI() != null ? request.getRequestURI() : "";
        String query = request.getQueryString() != null ? request.getQueryString() : "";
        return uri.contains("git-receive-pack") || query.contains("service=git-receive-pack");
    }
}
