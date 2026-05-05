package tn.enicarthage.qartnet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.enicarthage.qartnet.model.Reply;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    Optional<Reply> findByPublicId(UUID publicId);
    List<Reply> findByThreadPublicIdOrderByCreatedAtAsc(UUID threadPublicId);
    int countByThreadPublicId(UUID threadPublicId);
}
