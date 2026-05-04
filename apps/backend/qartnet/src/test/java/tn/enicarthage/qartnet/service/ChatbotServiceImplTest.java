package tn.enicarthage.qartnet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.enicarthage.qartnet.ai.GeminiClient;
import tn.enicarthage.qartnet.ai.GeminiClient.HistoryTurn;
import tn.enicarthage.qartnet.config.GeminiProperties;
import tn.enicarthage.qartnet.dto.request.AskChatbotRequest;
import tn.enicarthage.qartnet.dto.response.AskChatbotResponse;
import tn.enicarthage.qartnet.model.ChatConversation;
import tn.enicarthage.qartnet.model.ChatMessage;
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.repository.ChatConversationRepository;
import tn.enicarthage.qartnet.repository.ChatMessageRepository;
import tn.enicarthage.qartnet.repository.UserRepository;
import tn.enicarthage.qartnet.service.impl.ChatbotServiceImpl;
import tn.enicarthage.qartnet.shared.enums.ChatRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceImplTest {

    @Mock ChatConversationRepository conversationRepo;
    @Mock ChatMessageRepository messageRepo;
    @Mock UserRepository userRepo;
    @Mock GeminiClient gemini;

    GeminiProperties props = new GeminiProperties("k", null, null, "system", 20, 15);

    ChatbotServiceImpl service;

    private User alice;

    @BeforeEach
    void setUp() {
        service = new ChatbotServiceImpl(conversationRepo, messageRepo, userRepo, gemini, props);

        alice = new User();
        alice.setId(1L);
        alice.setPublicId(UUID.randomUUID());
    }

    @Test
    void ask_persistsBothTurnsAndForwardsHistoryToGemini() {
        when(userRepo.findByPublicId(alice.getPublicId())).thenReturn(Optional.of(alice));
        when(conversationRepo.findByOwnerAndActiveTrue(alice)).thenReturn(Optional.empty());

        ChatConversation conv = new ChatConversation();
        conv.setId(10L);
        conv.setOwner(alice);
        conv.setActive(true);
        conv.setPublicId(UUID.randomUUID());
        when(conversationRepo.save(any(ChatConversation.class))).thenReturn(conv);

        // Two pre-existing turns to verify history is forwarded.
        ChatMessage prevUser = new ChatMessage();
        prevUser.setRole(ChatRole.USER);
        prevUser.setContent("hi");
        ChatMessage prevModel = new ChatMessage();
        prevModel.setRole(ChatRole.MODEL);
        prevModel.setContent("hello!");

        // First save = newly persisted user turn; subsequent reads include it.
        List<ChatMessage> stored = new ArrayList<>(List.of(prevUser, prevModel));
        when(messageRepo.save(any(ChatMessage.class))).thenAnswer(inv -> {
            ChatMessage m = inv.getArgument(0);
            m.setPublicId(UUID.randomUUID());
            stored.add(m);
            return m;
        });
        when(messageRepo.findByConversationOrderByCreatedAtAsc(conv)).thenReturn(stored);

        when(gemini.generate(anyList())).thenReturn("answer");

        AskChatbotResponse res = service.ask(
                new AskChatbotRequest("how do I use Spring?"), alice.getPublicId().toString());

        verify(messageRepo, times(2)).save(any(ChatMessage.class));
        assertThat(res.userMessage().role()).isEqualTo(ChatRole.USER);
        assertThat(res.userMessage().content()).isEqualTo("how do I use Spring?");
        assertThat(res.assistantMessage().role()).isEqualTo(ChatRole.MODEL);
        assertThat(res.assistantMessage().content()).isEqualTo("answer");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<HistoryTurn>> captor = ArgumentCaptor.forClass(List.class);
        verify(gemini).generate(captor.capture());
        List<HistoryTurn> sent = captor.getValue();
        assertThat(sent).hasSize(3);
        assertThat(sent.get(2).role()).isEqualTo(ChatRole.USER);
        assertThat(sent.get(2).content()).isEqualTo("how do I use Spring?");
    }

    @Test
    void resetConversation_marksActiveOneInactive() {
        when(userRepo.findByPublicId(alice.getPublicId())).thenReturn(Optional.of(alice));
        ChatConversation conv = new ChatConversation();
        conv.setOwner(alice);
        conv.setActive(true);
        when(conversationRepo.findByOwnerAndActiveTrue(alice)).thenReturn(Optional.of(conv));

        service.resetConversation(alice.getPublicId().toString());

        assertThat(conv.isActive()).isFalse();
        verify(conversationRepo).save(conv);
    }
}
