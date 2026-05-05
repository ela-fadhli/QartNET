package tn.enicarthage.qartnet.service;

import tn.enicarthage.qartnet.dto.request.SendMessageRequest;
import tn.enicarthage.qartnet.dto.response.ConversationSummaryResponse;
import tn.enicarthage.qartnet.dto.response.MessageResponse;

import java.util.List;
import java.util.UUID;

public interface MessagingService {

    List<ConversationSummaryResponse> listConversations(String username);

    ConversationSummaryResponse getOrCreateDirectConversation(UUID otherUserPublicId, String username);

    List<MessageResponse> listMessages(UUID conversationPublicId, String beforeIso, int limit, String username);

    MessageResponse sendMessage(UUID conversationPublicId, SendMessageRequest req, String username);

    void markRead(UUID conversationPublicId, String username);
}
