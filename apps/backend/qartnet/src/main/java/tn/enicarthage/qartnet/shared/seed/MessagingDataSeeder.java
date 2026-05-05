package tn.enicarthage.qartnet.shared.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.qartnet.model.Conversation;
import tn.enicarthage.qartnet.model.ConversationParticipant;
import tn.enicarthage.qartnet.model.Message;
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.repository.ConversationParticipantRepository;
import tn.enicarthage.qartnet.repository.ConversationRepository;
import tn.enicarthage.qartnet.repository.MessageRepository;
import tn.enicarthage.qartnet.repository.UserRepository;
import tn.enicarthage.qartnet.shared.enums.ConversationType;

/**
 * Seeds two direct conversations between the users created by ForumDataSeeder.
 * Runs after ForumDataSeeder via @Order, and is idempotent through its own
 * guard on the conversations table — independent of the forum guard so it can
 * fill in messaging data on databases that already have forum content.
 */
@Component
@org.springframework.context.annotation.Profile("local")
@Order(20)
@RequiredArgsConstructor
public class MessagingDataSeeder implements ApplicationRunner {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (conversationRepository.count() > 0) return;

        User yassine = userRepository.findByEmail("yassine.benali@enicarthage.tn").orElse(null);
        User mariem  = userRepository.findByEmail("mariem.trabelsi@enicarthage.tn").orElse(null);
        User ahmed   = userRepository.findByEmail("ahmed.kefi@enicarthage.tn").orElse(null);
        if (yassine == null || mariem == null || ahmed == null) return;

        Conversation yassineMariem = seedDirectConversation(yassine, mariem);
        seedMessage(yassineMariem, mariem, "Salut Yassine! Did you see the PFE deadline got pushed?");
        seedMessage(yassineMariem, yassine, "Yes, I saw it on the platform. Honestly a relief.");
        seedMessage(yassineMariem, mariem, "Same. Want to pair on the architecture diagram tomorrow?");
        seedMessage(yassineMariem, yassine, "Sure, library at 10?");

        Conversation yassineAhmed = seedDirectConversation(yassine, ahmed);
        seedMessage(yassineAhmed, ahmed, "Did you finish the Spring Security setup?");
        seedMessage(yassineAhmed, yassine, "Almost — JWT works, fighting CORS now.");
        seedMessage(yassineAhmed, ahmed, "Classic. Send me the SecurityConfig when you can.");
    }

    private Conversation seedDirectConversation(User a, User b) {
        Conversation conv = new Conversation();
        conv.setType(ConversationType.DIRECT);
        conv.setCreatedBy(a);
        conversationRepository.save(conv);

        ConversationParticipant pa = new ConversationParticipant();
        pa.setConversation(conv);
        pa.setUser(a);
        conversationParticipantRepository.save(pa);

        ConversationParticipant pb = new ConversationParticipant();
        pb.setConversation(conv);
        pb.setUser(b);
        conversationParticipantRepository.save(pb);

        return conv;
    }

    private Message seedMessage(Conversation conv, User sender, String body) {
        Message m = new Message();
        m.setConversation(conv);
        m.setSender(sender);
        m.setBody(body);
        return messageRepository.save(m);
    }
}
