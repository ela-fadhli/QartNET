package tn.enicarthage.qartnet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.enicarthage.qartnet.model.Conversation;
import tn.enicarthage.qartnet.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByPublicId(UUID publicId);

    @Query("""
           SELECT c FROM Conversation c
           WHERE c.type = tn.enicarthage.qartnet.shared.enums.ConversationType.DIRECT
             AND (SELECT COUNT(p) FROM ConversationParticipant p WHERE p.conversation = c) = 2
             AND EXISTS (SELECT 1 FROM ConversationParticipant p1 WHERE p1.conversation = c AND p1.user = :a)
             AND EXISTS (SELECT 1 FROM ConversationParticipant p2 WHERE p2.conversation = c AND p2.user = :b)
           """)
    Optional<Conversation> findDirectBetween(@Param("a") User a, @Param("b") User b);

    @Query("""
           SELECT DISTINCT c FROM Conversation c
           JOIN c.participants p
           WHERE p.user = :user
           ORDER BY c.updatedAt DESC
           """)
    List<Conversation> findAllForUser(@Param("user") User user);
}
