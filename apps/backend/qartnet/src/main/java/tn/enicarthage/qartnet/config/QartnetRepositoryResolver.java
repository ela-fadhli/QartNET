package tn.enicarthage.qartnet.config;

import tn.enicarthage.qartnet.service.GitStorageService;
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

    public QartnetRepositoryResolver(GitStorageService gitStorageService) {
        this.gitStorageService = gitStorageService;
    }

    @Override
    public Repository open(HttpServletRequest request, String name) 
            throws RepositoryNotFoundException, ServiceNotAuthorizedException, ServiceNotEnabledException {
        
        // "name" correspond au chemin après /git/ (ex: username/project.git)
        File repoDir = gitStorageService.getBasePath().resolve(name).toFile();
        
        if (!repoDir.exists() || !repoDir.isDirectory()) {
            throw new RepositoryNotFoundException(name);
        }

        try {
            return new FileRepository(repoDir);
        } catch (IOException e) {
            throw new RepositoryNotFoundException(name, e);
        }
    }
}
