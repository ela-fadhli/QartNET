package tn.enicarthage.qartnet.shared.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.qartnet.model.*;
import tn.enicarthage.qartnet.repository.*;
import tn.enicarthage.qartnet.shared.enums.AccountStatus;
import tn.enicarthage.qartnet.shared.enums.Role;

import java.util.List;

@Component
@org.springframework.context.annotation.Profile("local")
@RequiredArgsConstructor
public class ForumDataSeeder implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ForumThreadRepository threadRepository;
    private final ReplyRepository replyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (categoryRepository.count() > 0) return;

        // --- Categories ---
        var enic     = save(category("ENICarthage Life", "Campus life, clubs, and student discussions"));
        var tech     = save(category("Software Engineering", "Web, backend, mobile, and DevOps"));
        var aiCat    = save(category("AI & Data تونس", "Machine learning and AI in Tunisia"));
        var career   = save(category("Internships & Careers", "PFE, internships, and jobs"));
        var startup  = save(category("Startups & Innovation", "Entrepreneurship and ideas"));
        var cafe     = save(category("Café", "Chill discussions between engineers"));

        // --- Tags ---
        var spring     = save(tag("spring-boot"));
        var angular    = save(tag("angular"));
        var ai         = save(tag("ai"));
        var devops     = save(tag("devops"));
        var pfe        = save(tag("pfe"));
        var internship = save(tag("internship"));
        var tunisia    = save(tag("tunisia"));
        var enicTag    = save(tag("enicarthage"));
        var startupTag = save(tag("startup"));
        var linux      = save(tag("linux"));

        // --- Users ---
        var yassine = seedUser("yassine.benali@enicarthage.tn", "yassine_dev", "Yassine", "Ben Ali");
        var mariem  = seedUser("mariem.trabelsi@enicarthage.tn", "mariem_ai", "Mariem", "Trabelsi");
        var ahmed   = seedUser("ahmed.kefi@enicarthage.tn", "ahmed_sys", "Ahmed", "Kefi");

        // --- Threads & Replies ---

        var t1 = seedThread(yassine, tech,
                "Spring Boot + Angular PFE — good stack?",
                "I'm planning my PFE using Spring Boot and Angular. Is it still relevant in Tunisia?",
                List.of(spring, angular, pfe, tunisia));

        seedReply(mariem, t1,
                "Yes, it's one of the most requested stacks in Tunisian companies.", null);

        seedReply(ahmed, t1,
                "Add Docker and CI/CD if you can — big plus for recruiters.", null);


        var t2 = seedThread(mariem, aiCat,
                "Best way to start with AI as an ENIC student?",
                "We studied math but I struggle applying it in real ML projects.",
                List.of(ai, enicTag));

        seedReply(yassine, t2,
                "Start with small Kaggle datasets then move to real-world use cases.", null);

        seedReply(ahmed, t2,
                "Try building something practical like a recommender system.", null);


        var t3 = seedThread(ahmed, career,
                "Where did you do your internship in Tunisia?",
                "Looking for a summer internship (stage). Any good companies?",
                List.of(internship, tunisia));

        seedReply(yassine, t3,
                "I did mine in a startup in Lac 1 — learned a lot.", null);

        seedReply(mariem, t3,
                "Startups > big companies for learning speed honestly.", null);


        var t4 = seedThread(yassine, startup,
                "Any ENIC students working on startups?",
                "Thinking about launching a SaaS product. Anyone interested?",
                List.of(startupTag, enicTag));

        seedReply(mariem, t4,
                "I'm working on an AI healthcare project 👀", null);


        var t5 = seedThread(mariem, enic,
                "How do you manage workload at ENICarthage?",
                "Between exams, projects, and self-learning it's overwhelming.",
                List.of(enicTag));

        seedReply(ahmed, t5,
                "You don't manage it… you survive it 😅", null);

        seedReply(yassine, t5,
                "Time-blocking and prioritization helped me a lot.", null);


        var t6 = seedThread(ahmed, tech,
                "Best Linux distro for dev in 2026?",
                "Ubuntu vs Arch vs Fedora — what are you using?",
                List.of(linux));

        seedReply(yassine, t6,
                "Ubuntu for stability, Arch if you want pain + learning 😂", null);


        var t7 = seedThread(yassine, cafe,
                "Best coffee spots near ENIC?",
                "Where do you usually study or chill?",
                List.of(tunisia));

        seedReply(mariem, t7,
                "Lac 2 cafés are the default answer 😄", null);
    }

    // --- Helpers ---

    private ForumCategory category(String name, String description) {
        ForumCategory c = new ForumCategory();
        c.setName(name);
        c.setDescription(description);
        return c;
    }

    private ForumCategory save(ForumCategory c) {
        return categoryRepository.save(c);
    }

    private Tag tag(String name) {
        Tag t = new Tag();
        t.setName(name);
        return t;
    }

    private Tag save(Tag t) {
        return tagRepository.save(t);
    }

    private User seedUser(String email, String username, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("Test1234!"));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmailVerified(true);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setRole(Role.USER);
        userRepository.save(user);

        Profile profile = new Profile();
        profile.setUser(user);
        profile.setUsername(username);
        profileRepository.save(profile);

        return user;
    }

    private ForumThread seedThread(User author, ForumCategory forumCategory, String title, String body, List<Tag> tags) {
        ForumThread t = new ForumThread();
        t.setTitle(title);
        t.setBody(body);
        t.setAuthor(author);
        t.setForumCategory(forumCategory);
        t.getTags().addAll(tags);
        return threadRepository.save(t);
    }

    private void seedReply(User author, ForumThread thread, String body, Reply parent) {
        Reply r = new Reply();
        r.setBody(body);
        r.setAuthor(author);
        r.setThread(thread);
        r.setParentReply(parent);
        replyRepository.save(r);
    }
}