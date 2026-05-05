package tn.enicarthage.qartnet.dto.response;

import tn.enicarthage.qartnet.shared.enums.ConversationType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ConversationSummaryResponse(
        UUID publicId,
        ConversationType type,
        String name,
        List<ConversationParticipantResponse> participants,
        MessageResponse lastMessage,
        long unreadCount,
        LocalDateTime updatedAt
) {}
