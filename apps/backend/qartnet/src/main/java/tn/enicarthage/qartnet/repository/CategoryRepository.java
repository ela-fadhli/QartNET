package tn.enicarthage.qartnet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.enicarthage.qartnet.model.ForumCategory;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<ForumCategory, Long> {

    Optional<ForumCategory> findByPublicId(UUID publicId);
}
