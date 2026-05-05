package tn.enicarthage.qartnet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.qartnet.dto.response.NotificationResponse;
import tn.enicarthage.qartnet.model.Notification;
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.repository.NotificationRepository;
import tn.enicarthage.qartnet.repository.UserRepository;
import tn.enicarthage.qartnet.service.NotificationService;
import tn.enicarthage.qartnet.shared.enums.NotificationType;
import tn.enicarthage.qartnet.shared.exception.ForbiddenException;
import tn.enicarthage.qartnet.shared.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepo;
    private final UserRepository userRepo;
    private final SimpMessagingTemplate broker;

    private static final int MAX_LIMIT = 100;

    @Override
    @Transactional
    public void notify(User recipient, NotificationType type, String title, String body, String link) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setType(type);
        n.setTitle(title);
        n.setBody(body);
        n.setLink(link);
        notificationRepo.save(n);

        broker.convertAndSendToUser(
                recipient.getPublicId().toString(),
                "/queue/notifications",
                toResponse(n));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> list(String username, boolean unreadOnly, int limit) {
        User me = requireUser(username);
        Pageable pageable = PageRequest.of(0, Math.min(limit <= 0 ? 20 : limit, MAX_LIMIT));
        return notificationRepo.findByRecipientOrderByCreatedAtDesc(me, pageable).stream()
                .filter(n -> !unreadOnly || n.getReadAt() == null)
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(String username) {
        return notificationRepo.countByRecipientAndReadAtIsNull(requireUser(username));
    }

    @Override
    @Transactional
    public void markRead(UUID publicId, String username) {
        User me = requireUser(username);
        Notification n = notificationRepo.findByPublicId(publicId)
                .orElseThrow(() -> ResourceNotFoundException.of("Notification", publicId));
        if (!n.getRecipient().getId().equals(me.getId()))
            throw new ForbiddenException("Cannot read another user's notification");
        if (n.getReadAt() == null) {
            n.setReadAt(LocalDateTime.now());
            notificationRepo.save(n);
        }
    }

    @Override
    @Transactional
    public void markAllRead(String username) {
        notificationRepo.markAllRead(requireUser(username));
    }

    // ── Private helpers ──────────────────────────────────────────────

    private User requireUser(String publicIdStr) {
        return userRepo.findByPublicId(UUID.fromString(publicIdStr))
                .orElseThrow(() -> ResourceNotFoundException.of("User", publicIdStr));
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getPublicId(),
                n.getType(),
                n.getTitle(),
                n.getBody(),
                n.getLink(),
                n.getReadAt() != null,
                n.getCreatedAt()
        );
    }
}
