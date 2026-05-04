package tn.enicarthage.qartnet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tn.enicarthage.qartnet.dto.request.RepositoryAccessRequest;
import tn.enicarthage.qartnet.model.*;
import tn.enicarthage.qartnet.repository.RepositoryAccessJpaRepository;
import tn.enicarthage.qartnet.repository.RepositoryJpaRepository;
import tn.enicarthage.qartnet.repository.UserRepository;
import tn.enicarthage.qartnet.shared.exception.ForbiddenException;
import tn.enicarthage.qartnet.shared.exception.ResourceNotFoundException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RepositoryAccessService {
    private final RepositoryJpaRepository repositoryJpaRepository;
    private final RepositoryAccessJpaRepository repositoryAccessJpaRepository;
    private final UserRepository userRepository;

    public RepositoryEntity getRepositoryOrThrow(String owner, String name) {
        return repositoryJpaRepository.findByOwnerIgnoreCaseAndNameIgnoreCase(owner, name)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found"));
    }

    public void ensureReadAccess(RepositoryEntity repository) {
        if (repository.getVisibility() == RepositoryVisibility.PUBLIC) return;
        if (!hasReadAccess(repository)) throw new ForbiddenException("Read access denied");
    }

    public void ensureWriteAccess(RepositoryEntity repository) {
        if (!hasWriteAccess(repository)) throw new ForbiddenException("Write access denied");
    }

    public boolean hasReadAccess(RepositoryEntity repository) {
        return hasMatchingAccess(repository, false);
    }

    public boolean hasWriteAccess(RepositoryEntity repository) {
        return hasMatchingAccess(repository, true);
    }

    public void bootstrapOwnerAccess(RepositoryEntity repository) {
        List<RepositoryAccessEntity> entries = repositoryAccessJpaRepository.findByRepositoryId(repository.getId());
        if (!entries.isEmpty()) return;
        repositoryAccessJpaRepository.save(RepositoryAccessEntity.builder()
                .repository(repository)
                .actorKey(repository.getOwner())
                .role(RepositoryAccessRole.OWNER)
                .level(RepositoryAccessLevel.WRITE)
                .build());
    }

    public List<RepositoryAccessEntity> listAccess(String owner, String name) {
        RepositoryEntity repository = getRepositoryOrThrow(owner, name);
        ensureWriteAccess(repository);
        return repositoryAccessJpaRepository.findByRepositoryId(repository.getId());
    }

    public RepositoryAccessEntity upsertAccess(String owner, String name, RepositoryAccessRequest request) {
        RepositoryEntity repository = getRepositoryOrThrow(owner, name);
        ensureWriteAccess(repository);
        String actorKey = sanitizeActorKey(request.getActorKey());
        RepositoryAccessEntity entry = repositoryAccessJpaRepository
                .findByRepositoryIdAndActorKeyIgnoreCase(repository.getId(), actorKey)
                .orElse(RepositoryAccessEntity.builder().repository(repository).actorKey(actorKey).build());
        entry.setRole(request.getRole());
        entry.setLevel(request.getLevel());
        return repositoryAccessJpaRepository.save(entry);
    }

    public void revokeAccess(String owner, String name, String actorKey) {
        RepositoryEntity repository = getRepositoryOrThrow(owner, name);
        ensureWriteAccess(repository);
        if (repository.getOwner().equalsIgnoreCase(actorKey)) {
            throw new ForbiddenException("Owner access cannot be revoked");
        }
        repositoryAccessJpaRepository.deleteByRepositoryIdAndActorKeyIgnoreCase(repository.getId(), actorKey);
    }

    public boolean canReadByActorKey(RepositoryEntity repository, String actorKey) {
        return hasAccessForKey(repository, sanitizeActorKey(actorKey), false);
    }

    public boolean canWriteByActorKey(RepositoryEntity repository, String actorKey) {
        return hasAccessForKey(repository, sanitizeActorKey(actorKey), true);
    }

    private boolean hasMatchingAccess(RepositoryEntity repository, boolean writeRequired) {
        Set<String> actorKeys = getCurrentActorKeys();
        if (actorKeys.isEmpty()) return false;
        for (String actorKey : actorKeys) {
            if (hasAccessForKey(repository, actorKey, writeRequired)) return true;
        }
        return false;
    }

    private boolean hasAccessForKey(RepositoryEntity repository, String actorKey, boolean writeRequired) {
        if (actorKey == null || actorKey.isBlank()) return false;
        if (repository.getOwner() != null && repository.getOwner().equalsIgnoreCase(actorKey)) return true;

        Optional<RepositoryAccessEntity> access = repositoryAccessJpaRepository
                .findByRepositoryIdAndActorKeyIgnoreCase(repository.getId(), actorKey);
        if (access.isEmpty()) return false;
        return !writeRequired || access.get().getLevel() == RepositoryAccessLevel.WRITE;
    }

    private Set<String> getCurrentActorKeys() {
        Set<String> keys = new HashSet<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return keys;
        }

        String principal = auth.getName();
        if (principal != null && !principal.isBlank()) {
            keys.add(principal.trim().toLowerCase());
            try {
                UUID id = UUID.fromString(principal);
                userRepository.findByPublicId(id).ifPresent(user -> {
                    if (user.getEmail() != null) {
                        String email = user.getEmail().trim().toLowerCase();
                        keys.add(email);
                        int at = email.indexOf('@');
                        if (at > 0) keys.add(email.substring(0, at));
                    }
                });
            } catch (IllegalArgumentException ignored) {
                // Principal may not be a UUID in non-JWT contexts.
            }
        }
        return keys;
    }

    private String sanitizeActorKey(String actorKey) {
        if (actorKey == null || actorKey.isBlank()) {
            throw new ForbiddenException("actorKey is required");
        }
        return actorKey.trim().toLowerCase();
    }
}
