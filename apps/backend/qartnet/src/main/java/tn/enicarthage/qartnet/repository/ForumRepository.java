package tn.enicarthage.qartnet.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.enicarthage.qartnet.model.Forum;

import java.util.Optional;
import java.util.UUID;

public interface ForumRepository extends JpaRepository<Forum, Long> {
    Optional<Forum> findByPublicId(UUID publicId);
    Optional<Forum> findBySlug(String slug);
    boolean existsBySlug(String slug);
    Page<Forum> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
