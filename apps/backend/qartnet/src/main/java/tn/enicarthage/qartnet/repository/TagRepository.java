package tn.enicarthage.qartnet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.enicarthage.qartnet.model.Tag;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByPublicId(UUID publicId);
    List<Tag> findByPublicIdIn(List<UUID> publicIds);
}
