package tn.enicarthage.qartnet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.enicarthage.qartnet.model.Forum;
import tn.enicarthage.qartnet.model.ForumCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ForumCategoryRepository extends JpaRepository<ForumCategory, Long> {
    List<ForumCategory> findByForum(Forum forum);
    Optional<ForumCategory> findByPublicIdAndForum(UUID publicId, Forum forum);
    boolean existsByNameIgnoreCaseAndForum(String name, Forum forum);
}
