package tn.enicarthage.qartnet.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.enicarthage.qartnet.model.Conversation;
import tn.enicarthage.qartnet.model.Message;
import tn.enicarthage.qartnet.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Optional<Message> findByPublicId(UUID publicId);

    List<Message> findByConversationOrderByCreatedAtDesc(Conversation conversation, Pageable pageable);

    List<Message> findByConversationAndCreatedAtBeforeOrderByCreatedAtDesc(
            Conversation conversation, LocalDateTime before, Pageable pageable);

    Optional<Message> findFirstByConversationOrderByCreatedAtDesc(Conversation conversation);

    long countByConversationAndSenderNot(Conversation conversation, User viewer);

    long countByConversationAndSenderNotAndCreatedAtAfter(
            Conversation conversation,
            User viewer,
            LocalDateTime after
    );
}
