package tn.enicarthage.qartnet.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.enicarthage.qartnet.model.Forum;
import tn.enicarthage.qartnet.model.ForumCategory;
import tn.enicarthage.qartnet.model.ForumThread;
import java.util.Optional;
import java.util.UUID;

public interface ForumThreadRepository extends JpaRepository<ForumThread, Long> {
    Optional<ForumThread> findByPublicId(UUID publicId);
    Page<ForumThread> findByForum(Forum forum, Pageable pageable);
    Page<ForumThread> findByForumAndCategory(Forum forum, ForumCategory category, Pageable pageable);
    long countByForum(Forum forum);
}

