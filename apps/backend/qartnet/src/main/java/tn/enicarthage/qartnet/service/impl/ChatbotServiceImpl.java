package tn.enicarthage.qartnet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.qartnet.ai.GeminiClient;
import tn.enicarthage.qartnet.ai.GeminiClient.HistoryTurn;
import tn.enicarthage.qartnet.config.GeminiProperties;
import tn.enicarthage.qartnet.dto.request.AskChatbotRequest;
import tn.enicarthage.qartnet.dto.response.AskChatbotResponse;
import tn.enicarthage.qartnet.dto.response.ChatMessageResponse;
import tn.enicarthage.qartnet.model.ChatConversation;
import tn.enicarthage.qartnet.model.ChatMessage;
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.repository.ChatConversationRepository;
import tn.enicarthage.qartnet.repository.ChatMessageRepository;
import tn.enicarthage.qartnet.repository.UserRepository;
import tn.enicarthage.qartnet.service.ChatbotService;
import tn.enicarthage.qartnet.shared.enums.ChatRole;
import tn.enicarthage.qartnet.shared.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {

    private final ChatConversationRepository conversationRepo;
    private final ChatMessageRepository messageRepo;
    private final UserRepository userRepo;
    private final GeminiClient gemini;
    private final GeminiProperties props;

    @Override
    @Transactional
    public AskChatbotResponse ask(AskChatbotRequest request, String username) {
        User me = requireUser(username);
        ChatConversation conv = getOrCreateActive(me);

        ChatMessage userTurn = saveMessage(conv, ChatRole.USER, request.message());

        List<ChatMessage> history = messageRepo.findByConversationOrderByCreatedAtAsc(conv);
        List<HistoryTurn> turns = history.stream()
                .skip(Math.max(0, history.size() - props.historyLimit()))
                .map(m -> new HistoryTurn(m.getRole(), m.getContent()))
                .toList();

        String reply = gemini.generate(turns);

        ChatMessage assistantTurn = saveMessage(conv, ChatRole.MODEL, reply);

        return new AskChatbotResponse(toResponse(userTurn), toResponse(assistantTurn));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getHistory(String username) {
        User me = requireUser(username);
        return conversationRepo.findByOwnerAndActiveTrue(me)
                .map(c -> messageRepo.findByConversationOrderByCreatedAtAsc(c).stream()
                        .map(this::toResponse).toList())
                .orElse(List.of());
    }

    @Override
    @Transactional
    public void resetConversation(String username) {
        User me = requireUser(username);
        conversationRepo.findByOwnerAndActiveTrue(me).ifPresent(c -> {
            c.setActive(false);
            conversationRepo.save(c);
        });
    }

    // ── Private helpers ──────────────────────────────────────────────

    private User requireUser(String publicIdStr) {
        return userRepo.findByPublicId(UUID.fromString(publicIdStr))
                .orElseThrow(() -> ResourceNotFoundException.of("User", publicIdStr));
    }

    private ChatConversation getOrCreateActive(User owner) {
        return conversationRepo.findByOwnerAndActiveTrue(owner).orElseGet(() -> {
            ChatConversation c = new ChatConversation();
            c.setOwner(owner);
            c.setActive(true);
            return conversationRepo.save(c);
        });
    }

    private ChatMessage saveMessage(ChatConversation conv, ChatRole role, String content) {
        ChatMessage m = new ChatMessage();
        m.setConversation(conv);
        m.setRole(role);
        m.setContent(content);
        return messageRepo.save(m);
    }

    private ChatMessageResponse toResponse(ChatMessage m) {
        return new ChatMessageResponse(m.getPublicId(), m.getRole(), m.getContent(), m.getCreatedAt());
    }
}
