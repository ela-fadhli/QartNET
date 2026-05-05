package tn.enicarthage.qartnet.dto.response;

import java.util.UUID;

public record ConversationParticipantResponse(
        UUID userPublicId,
        String username,
        String profilePictureUrl
) {}
