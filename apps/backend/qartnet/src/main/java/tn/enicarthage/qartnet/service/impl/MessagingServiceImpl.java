package tn.enicarthage.qartnet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.qartnet.dto.request.SendMessageRequest;
import tn.enicarthage.qartnet.dto.response.ConversationParticipantResponse;
import tn.enicarthage.qartnet.dto.response.ConversationSummaryResponse;
import tn.enicarthage.qartnet.dto.response.MessageResponse;
import tn.enicarthage.qartnet.model.Conversation;
import tn.enicarthage.qartnet.model.ConversationParticipant;
import tn.enicarthage.qartnet.model.Message;
import tn.enicarthage.qartnet.model.Profile;
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.repository.ConversationParticipantRepository;
import tn.enicarthage.qartnet.repository.ConversationRepository;
import tn.enicarthage.qartnet.repository.MessageRepository;
import tn.enicarthage.qartnet.repository.ProfileRepository;
import tn.enicarthage.qartnet.repository.UserRepository;
import tn.enicarthage.qartnet.service.MessagingService;
import tn.enicarthage.qartnet.service.NotificationService;
import tn.enicarthage.qartnet.shared.enums.ConversationType;
import tn.enicarthage.qartnet.shared.enums.NotificationType;
import tn.enicarthage.qartnet.shared.exception.ConflictException;
import tn.enicarthage.qartnet.shared.exception.ForbiddenException;
import tn.enicarthage.qartnet.shared.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessagingServiceImpl implements MessagingService {

    private final ConversationRepository conversationRepo;
    private final ConversationParticipantRepository participantRepo;
    private final MessageRepository messageRepo;
    private final UserRepository userRepo;
    private final ProfileRepository profileRepo;
    private final SimpMessagingTemplate broker;
    private final NotificationService notificationService;

    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 100;

    @Override
    @Transactional(readOnly = true)
    public List<ConversationSummaryResponse> listConversations(String username) {
        User me = requireUser(username);
        return conversationRepo.findAllForUser(me).stream()
                .map(c -> toSummary(c, me))
                .sorted(Comparator.comparing(
                        (ConversationSummaryResponse s) ->
                                s.lastMessage() != null ? s.lastMessage().sentAt() : s.updatedAt())
                        .reversed())
                .toList();
    }

    @Override
    @Transactional
    public ConversationSummaryResponse getOrCreateDirectConversation(UUID otherUserPublicId, String username) {
        User me = requireUser(username);
        User other = userRepo.findByPublicId(otherUserPublicId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", otherUserPublicId));
        if (other.getId().equals(me.getId()))
            throw new ConflictException("Cannot start a conversation with yourself");

        Conversation conv = conversationRepo.findDirectBetween(me, other)
                .orElseGet(() -> createDirect(me, other));
        return toSummary(conv, me);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> listMessages(UUID conversationPublicId, String beforeIso, int limit, String username) {
        User me = requireUser(username);
        Conversation conv = requireConversation(conversationPublicId);
        requireParticipant(conv, me);

        int size = Math.min(limit <= 0 ? DEFAULT_PAGE_SIZE : limit, MAX_PAGE_SIZE);
        var pageable = PageRequest.of(0, size);

        List<Message> messages = (beforeIso == null || beforeIso.isBlank())
                ? messageRepo.findByConversationOrderByCreatedAtDesc(conv, pageable)
                : messageRepo.findByConversationAndCreatedAtBeforeOrderByCreatedAtDesc(
                        conv, LocalDateTime.parse(beforeIso), pageable);

        return messages.stream()
                .sorted(Comparator.comparing(Message::getCreatedAt))
                .map(this::toMessageResponse)
                .toList();
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(UUID conversationPublicId, SendMessageRequest req, String username) {
        User me = requireUser(username);
        Conversation conv = requireConversation(conversationPublicId);
        requireParticipant(conv, me);

        Message msg = new Message();
        msg.setConversation(conv);
        msg.setSender(me);
        msg.setBody(req.body());
        messageRepo.save(msg);

        // Touch conversation so listConversations ordering and read-tracking stay correct.
        conversationRepo.save(conv);

        MessageResponse payload = toMessageResponse(msg);

        broker.convertAndSend("/topic/conversation." + conv.getPublicId(), payload);
        String snippet = req.body().length() > 120
                ? req.body().substring(0, 120) + "…" : req.body();
        for (ConversationParticipant p : participantRepo.findByConversation(conv)) {
            broker.convertAndSendToUser(
                    p.getUser().getPublicId().toString(),
                    "/queue/messages",
                    payload);
            if (!p.getUser().getId().equals(me.getId())) {
                notificationService.notify(
                        p.getUser(),
                        NotificationType.MESSAGE,
                        "New message from " + usernameOf(me),
                        snippet,
                        "/messaging/" + conv.getPublicId());
            }
        }

        return payload;
    }

    @Override
    @Transactional
    public void markRead(UUID conversationPublicId, String username) {
        User me = requireUser(username);
        Conversation conv = requireConversation(conversationPublicId);
        ConversationParticipant participant = participantRepo.findByConversationAndUser(conv, me)
                .orElseThrow(() -> new ForbiddenException("Not a participant of this conversation"));
        participant.setLastReadAt(LocalDateTime.now());
        participantRepo.save(participant);
    }

    // ── Private helpers ──────────────────────────────────────────────

    private Conversation createDirect(User a, User b) {
        Conversation conv = new Conversation();
        conv.setType(ConversationType.DIRECT);
        conv.setCreatedBy(a);
        conversationRepo.save(conv);

        addParticipant(conv, a);
        addParticipant(conv, b);
        return conv;
    }

    private void addParticipant(Conversation conv, User user) {
        ConversationParticipant p = new ConversationParticipant();
        p.setConversation(conv);
        p.setUser(user);
        participantRepo.save(p);
    }

    private User requireUser(String publicIdStr) {
        return userRepo.findByPublicId(UUID.fromString(publicIdStr))
                .orElseThrow(() -> ResourceNotFoundException.of("User", publicIdStr));
    }

    private Conversation requireConversation(UUID publicId) {
        return conversationRepo.findByPublicId(publicId)
                .orElseThrow(() -> ResourceNotFoundException.of("Conversation", publicId));
    }

    private void requireParticipant(Conversation conv, User user) {
        if (!participantRepo.existsByConversationAndUser(conv, user))
            throw new ForbiddenException("Not a participant of this conversation");
    }

    private String usernameOf(User user) {
        return profileRepo.findByUser(user).map(Profile::getUsername).orElse("unknown");
    }

    private String avatarOf(User user) {
        return profileRepo.findByUser(user).map(Profile::getProfilePictureUrl).orElse(null);
    }

    private MessageResponse toMessageResponse(Message m) {
        return new MessageResponse(
                m.getPublicId(),
                m.getConversation().getPublicId(),
                m.getSender().getPublicId(),
                usernameOf(m.getSender()),
                m.getBody(),
                m.getCreatedAt(),
                m.getEditedAt()
        );
    }

    private ConversationSummaryResponse toSummary(Conversation conv, User me) {
        List<ConversationParticipantResponse> participants = participantRepo.findByConversation(conv).stream()
                .map(p -> new ConversationParticipantResponse(
                        p.getUser().getPublicId(),
                        usernameOf(p.getUser()),
                        avatarOf(p.getUser())))
                .toList();

        Optional<Message> last = messageRepo.findFirstByConversationOrderByCreatedAtDesc(conv);
        MessageResponse lastMsg = last.map(this::toMessageResponse).orElse(null);

        ConversationParticipant myParticipant = participantRepo.findByConversationAndUser(conv, me)
                .orElseThrow(() -> new ForbiddenException("Not a participant of this conversation"));
        LocalDateTime lastReadAt = myParticipant.getLastReadAt();

        long unread = (lastReadAt == null)
                ? messageRepo.countByConversationAndSenderNot(conv, me)
                : messageRepo.countByConversationAndSenderNotAndCreatedAtAfter(conv, me, lastReadAt);

        return new ConversationSummaryResponse(
                conv.getPublicId(),
                conv.getType(),
                conv.getName(),
                participants,
                lastMsg,
                unread,
                conv.getUpdatedAt()
        );
    }
}
