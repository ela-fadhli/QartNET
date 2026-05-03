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
import tn.enicarthage.qartnet.shared.enums.ForumRole;
import tn.enicarthage.qartnet.shared.enums.Role;

import java.util.List;

@Component
@org.springframework.context.annotation.Profile("local")
@RequiredArgsConstructor
public class ForumDataSeeder implements ApplicationRunner {

    private final ForumRepository forumRepository;
    private final ForumCategoryRepository forumCategoryRepository;
    private final ForumMemberRepository forumMemberRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ForumThreadRepository threadRepository;
    private final ReplyRepository replyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (forumRepository.count() > 0) return;

        // --- Users ---
        User yassine = seedUser("yassine.benali@enicarthage.tn", "yassine_dev", "Yassine", "Ben Ali");
        User mariem  = seedUser("mariem.trabelsi@enicarthage.tn", "mariem_ai",  "Mariem",  "Trabelsi");
        User ahmed   = seedUser("ahmed.kefi@enicarthage.tn",      "ahmed_sys",  "Ahmed",   "Kefi");

        // --- Tags (global) ---
        Tag spring     = saveTag("spring-boot");
        Tag angular    = saveTag("angular");
        Tag ai         = saveTag("ai");
        Tag pfe        = saveTag("pfe");
        Tag internship = saveTag("internship");
        Tag tunisia    = saveTag("tunisia");
        Tag enicTag    = saveTag("enicarthage");
        Tag startupTag = saveTag("startup");
        Tag linux      = saveTag("linux");

        // ── Forum 1: ENICarthage Hub (owned by yassine) ──────────────

        Forum enicHub = saveForum("ENICarthage Hub", "enicarthage-hub",
                "The official hub for ENICarthage students — campus life, projects, and more.", yassine);

        ForumCategory campusLife = saveCategory("Campus Life",        enicHub);
        ForumCategory softwareEng = saveCategory("Software Engineering", enicHub);
        ForumCategory aiData      = saveCategory("AI & Data",            enicHub);

        // yassine is owner → also an ADMIN member
        saveOwnerMember(enicHub, yassine);
        // mariem is a moderator
        saveMember(enicHub, mariem, ForumRole.MODERATOR);

        var t1 = seedThread(yassine, enicHub, softwareEng,
                "Spring Boot + Angular PFE — good stack?",
                "I'm planning my PFE using Spring Boot and Angular. Is it still relevant in Tunisia?",
                List.of(spring, angular, pfe, tunisia));
        seedReply(mariem, t1, "Yes, it's one of the most requested stacks in Tunisian companies.", null);
        Reply r1 = seedReply(ahmed, t1, "Add Docker and CI/CD if you can — big plus for recruiters.", null);
        seedReply(yassine, t1, "Totally agree, I'll add Kubernetes too.", r1);

        var t2 = seedThread(mariem, enicHub, aiData,
                "Best way to start with AI as an ENIC student?",
                "We studied math but I struggle applying it in real ML projects.",
                List.of(ai, enicTag));
        seedReply(yassine, t2, "Start with small Kaggle datasets then move to real-world use cases.", null);
        seedReply(ahmed, t2, "Try building something practical like a recommender system.", null);

        var t3 = seedThread(ahmed, enicHub, campusLife,
                "How do you manage workload at ENICarthage?",
                "Between exams, projects, and self-learning it's overwhelming.",
                List.of(enicTag));
        seedReply(mariem, t3, "You don't manage it… you survive it 😅", null);
        seedReply(yassine, t3, "Time-blocking and prioritization helped me a lot.", null);

        // ── Forum 2: Tunisia Tech Network (owned by mariem) ──────────

        Forum tunisiaTech = saveForum("Tunisia Tech Network", "tunisia-tech",
                "A space for Tunisian engineers — internships, startups, and industry talk.", mariem);

        ForumCategory careers    = saveCategory("Internships & Careers", tunisiaTech);
        ForumCategory startups   = saveCategory("Startups & Innovation",  tunisiaTech);
        ForumCategory cafeCorner = saveCategory("Café Corner",             tunisiaTech);

        saveOwnerMember(tunisiaTech, mariem);
        saveMember(tunisiaTech, ahmed, ForumRole.ADMIN);

        var t4 = seedThread(ahmed, tunisiaTech, careers,
                "Where did you do your internship in Tunisia?",
                "Looking for a summer internship (stage). Any good companies?",
                List.of(internship, tunisia));
        seedReply(yassine, t4, "I did mine in a startup in Lac 1 — learned a lot.", null);
        seedReply(mariem, t4, "Startups > big companies for learning speed honestly.", null);

        var t5 = seedThread(yassine, tunisiaTech, startups,
                "Any ENIC students working on startups?",
                "Thinking about launching a SaaS product. Anyone interested in collaborating?",
                List.of(startupTag, enicTag));
        seedReply(mariem, t5, "I'm working on an AI healthcare project 👀", null);

        var t6 = seedThread(ahmed, tunisiaTech, cafeCorner,
                "Best Linux distro for dev in 2026?",
                "Ubuntu vs Arch vs Fedora — what are you using?",
                List.of(linux));
        seedReply(yassine, t6, "Ubuntu for stability, Arch if you want pain + learning 😂", null);
        seedReply(mariem, t6, "Fedora — best balance between fresh packages and stability.", null);
    }

    // --- Helpers ---

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

    private Tag saveTag(String name) {
        Tag t = new Tag();
        t.setName(name);
        return tagRepository.save(t);
    }

    private Forum saveForum(String name, String slug, String description, User owner) {
        Forum f = new Forum();
        f.setName(name);
        f.setSlug(slug);
        f.setDescription(description);
        f.setOwner(owner);
        return forumRepository.save(f);
    }

    private ForumCategory saveCategory(String name, Forum forum) {
        ForumCategory c = new ForumCategory();
        c.setName(name);
        c.setForum(forum);
        return forumCategoryRepository.save(c);
    }

    private void saveOwnerMember(Forum forum, User owner) {
        ForumMember m = new ForumMember();
        m.setForum(forum);
        m.setUser(owner);
        m.setRole(ForumRole.ADMIN);
        forumMemberRepository.save(m);
    }

    private void saveMember(Forum forum, User user, ForumRole role) {
        ForumMember m = new ForumMember();
        m.setForum(forum);
        m.setUser(user);
        m.setRole(role);
        forumMemberRepository.save(m);
    }

    private ForumThread seedThread(User author, Forum forum, ForumCategory category,
                                    String title, String body, List<Tag> tags) {
        ForumThread t = new ForumThread();
        t.setTitle(title);
        t.setBody(body);
        t.setAuthor(author);
        t.setForum(forum);
        t.setCategory(category);
        t.getTags().addAll(tags);
        return threadRepository.save(t);
    }

    private Reply seedReply(User author, ForumThread thread, String body, Reply parent) {
        Reply r = new Reply();
        r.setBody(body);
        r.setAuthor(author);
        r.setThread(thread);
        r.setParentReply(parent);
        return replyRepository.save(r);
    }
}
