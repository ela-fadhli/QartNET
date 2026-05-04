package tn.enicarthage.qartnet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import tn.enicarthage.qartnet.dto.request.SendMessageRequest;
import tn.enicarthage.qartnet.dto.response.ConversationSummaryResponse;
import tn.enicarthage.qartnet.model.Conversation;
import tn.enicarthage.qartnet.model.ConversationParticipant;
import tn.enicarthage.qartnet.model.Message;
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.repository.ConversationParticipantRepository;
import tn.enicarthage.qartnet.repository.ConversationRepository;
import tn.enicarthage.qartnet.repository.MessageRepository;
import tn.enicarthage.qartnet.repository.ProfileRepository;
import tn.enicarthage.qartnet.repository.UserRepository;
import tn.enicarthage.qartnet.service.impl.MessagingServiceImpl;
import tn.enicarthage.qartnet.shared.enums.ConversationType;
import tn.enicarthage.qartnet.shared.exception.ConflictException;
import tn.enicarthage.qartnet.shared.exception.ForbiddenException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessagingServiceImplTest {

    @Mock ConversationRepository conversationRepo;
    @Mock ConversationParticipantRepository participantRepo;
    @Mock MessageRepository messageRepo;
    @Mock UserRepository userRepo;
    @Mock ProfileRepository profileRepo;
    @Mock SimpMessagingTemplate broker;

    @InjectMocks MessagingServiceImpl service;

    private User alice;
    private User bob;
    private User intruder;
    private Conversation conv;

    @BeforeEach
    void setUp() {
        alice = new User();
        alice.setId(1L);
        alice.setPublicId(UUID.randomUUID());

        bob = new User();
        bob.setId(2L);
        bob.setPublicId(UUID.randomUUID());

        intruder = new User();
        intruder.setId(3L);
        intruder.setPublicId(UUID.randomUUID());

        conv = new Conversation();
        conv.setId(10L);
        conv.setPublicId(UUID.randomUUID());
        conv.setType(ConversationType.DIRECT);
    }

    @Test
    void sendMessage_persistsAndBroadcasts_whenSenderIsParticipant() {
        when(userRepo.findByPublicId(UUID.fromString(alice.getPublicId().toString())))
                .thenReturn(Optional.of(alice));
        when(conversationRepo.findByPublicId(conv.getPublicId())).thenReturn(Optional.of(conv));
        when(participantRepo.existsByConversationAndUser(conv, alice)).thenReturn(true);

        ConversationParticipant aliceP = new ConversationParticipant();
        aliceP.setConversation(conv);
        aliceP.setUser(alice);
        ConversationParticipant bobP = new ConversationParticipant();
        bobP.setConversation(conv);
        bobP.setUser(bob);
        when(participantRepo.findByConversation(conv)).thenReturn(List.of(aliceP, bobP));

        when(messageRepo.save(any(Message.class))).thenAnswer(inv -> {
            Message m = inv.getArgument(0);
            m.setPublicId(UUID.randomUUID());
            return m;
        });

        service.sendMessage(conv.getPublicId(), new SendMessageRequest("hello"), alice.getPublicId().toString());

        verify(messageRepo, times(1)).save(any(Message.class));
        verify(broker).convertAndSend(eq("/topic/conversation." + conv.getPublicId()), any(Object.class));
        verify(broker).convertAndSendToUser(
                eq(alice.getPublicId().toString()), eq("/queue/messages"), any(Object.class));
        verify(broker).convertAndSendToUser(
                eq(bob.getPublicId().toString()), eq("/queue/messages"), any(Object.class));
    }

    @Test
    void sendMessage_rejects_whenSenderIsNotParticipant() {
        when(userRepo.findByPublicId(intruder.getPublicId())).thenReturn(Optional.of(intruder));
        when(conversationRepo.findByPublicId(conv.getPublicId())).thenReturn(Optional.of(conv));
        when(participantRepo.existsByConversationAndUser(conv, intruder)).thenReturn(false);

        assertThatThrownBy(() -> service.sendMessage(
                conv.getPublicId(),
                new SendMessageRequest("hi"),
                intruder.getPublicId().toString()))
                .isInstanceOf(ForbiddenException.class);

        verify(messageRepo, never()).save(any(Message.class));
        verify(broker, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void getOrCreateDirectConversation_rejectsSelfConversation() {
        when(userRepo.findByPublicId(alice.getPublicId())).thenReturn(Optional.of(alice));

        assertThatThrownBy(() -> service.getOrCreateDirectConversation(
                alice.getPublicId(), alice.getPublicId().toString()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void getOrCreateDirectConversation_returnsExisting_whenAlreadyPresent() {
        when(userRepo.findByPublicId(alice.getPublicId())).thenReturn(Optional.of(alice));
        when(userRepo.findByPublicId(bob.getPublicId())).thenReturn(Optional.of(bob));
        when(conversationRepo.findDirectBetween(alice, bob)).thenReturn(Optional.of(conv));

        ConversationParticipant aliceP = new ConversationParticipant();
        aliceP.setConversation(conv);
        aliceP.setUser(alice);
        ConversationParticipant bobP = new ConversationParticipant();
        bobP.setConversation(conv);
        bobP.setUser(bob);
        when(participantRepo.findByConversation(conv)).thenReturn(List.of(aliceP, bobP));
        when(participantRepo.findByConversationAndUser(conv, alice)).thenReturn(Optional.of(aliceP));
        when(messageRepo.findFirstByConversationOrderByCreatedAtDesc(conv)).thenReturn(Optional.empty());
        when(messageRepo.countUnreadFor(eq(conv), eq(alice), any())).thenReturn(0L);

        ConversationSummaryResponse summary = service.getOrCreateDirectConversation(
                bob.getPublicId(), alice.getPublicId().toString());

        assertThat(summary.publicId()).isEqualTo(conv.getPublicId());
        verify(conversationRepo, never()).save(any(Conversation.class));
    }
}
