package tn.enicarthage.qartnet.service;

import tn.enicarthage.qartnet.dto.response.NotificationResponse;
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.shared.enums.NotificationType;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    /**
     * Persist a notification for the recipient and push it live over STOMP
     * to /user/queue/notifications. The sender is responsible for skipping
     * self-notifications before calling this method.
     */
    void notify(User recipient, NotificationType type, String title, String body, String link);

    List<NotificationResponse> list(String username, boolean unreadOnly, int limit);

    long unreadCount(String username);

    void markRead(UUID publicId, String username);

    void markAllRead(String username);
}
