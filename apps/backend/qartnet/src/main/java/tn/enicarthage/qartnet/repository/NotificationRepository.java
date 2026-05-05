package tn.enicarthage.qartnet.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.enicarthage.qartnet.model.Notification;
import tn.enicarthage.qartnet.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByPublicId(UUID publicId);

    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);

    long countByRecipientAndReadAtIsNull(User recipient);

    @Modifying
    @Query("""
           UPDATE Notification n
           SET n.readAt = CURRENT_TIMESTAMP
           WHERE n.recipient = :recipient AND n.readAt IS NULL
           """)
    int markAllRead(@Param("recipient") User recipient);
}
