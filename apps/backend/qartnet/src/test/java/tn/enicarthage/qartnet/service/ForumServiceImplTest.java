package tn.enicarthage.qartnet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import tn.enicarthage.qartnet.dto.request.*;
import tn.enicarthage.qartnet.dto.response.*;
import tn.enicarthage.qartnet.model.*;
import tn.enicarthage.qartnet.repository.*;
import tn.enicarthage.qartnet.service.impl.ForumServiceImpl;
import tn.enicarthage.qartnet.shared.enums.ForumRole;
import tn.enicarthage.qartnet.shared.exception.ConflictException;
import tn.enicarthage.qartnet.shared.exception.ForbiddenException;
import tn.enicarthage.qartnet.shared.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForumServiceImplTest {

    @Mock ForumRepository forumRepo;
    @Mock ForumMemberRepository memberRepo;
    @Mock ForumCategoryRepository categoryRepo;
    @Mock ForumThreadRepository threadRepo;
    @Mock ReplyRepository replyRepo;
    @Mock UserRepository userRepo;
    @Mock ProfileRepository profileRepo;
    @Mock TagRepository tagRepo;

    @InjectMocks ForumServiceImpl forumService;

    private User owner;
    private Profile ownerProfile;
    private User otherUser;
    private Profile otherProfile;
    private Forum forum;
    private ForumCategory category;
    private ForumThread thread;
    private Reply reply;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setPublicId(UUID.randomUUID());
        owner.setEmail("owner@test.com");

        ownerProfile = new Profile();
        ownerProfile.setUsername("owner_user");
        ownerProfile.setUser(owner);

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setPublicId(UUID.randomUUID());
        otherUser.setEmail("other@test.com");

        otherProfile = new Profile();
        otherProfile.setUsername("other_user");
        otherProfile.setUser(otherUser);

        forum = new Forum();
        forum.setId(1L);
        forum.setPublicId(UUID.randomUUID());
        forum.setName("Test Forum");
        forum.setSlug("test-forum");
        forum.setOwner(owner);

        category = new ForumCategory();
        category.setId(1L);
        category.setPublicId(UUID.randomUUID());
        category.setName("Tech");
        category.setForum(forum);

        thread = new ForumThread();
        thread.setId(1L);
        thread.setPublicId(UUID.randomUUID());
        thread.setTitle("Best practices");
        thread.setBody("What are the best practices for Spring Boot?");
        thread.setAuthor(owner);
        thread.setForum(forum);
        thread.setCategory(category);
        thread.setViewCount(0L);

        reply = new Reply();
        reply.setId(1L);
        reply.setPublicId(UUID.randomUUID());
        reply.setBody("Great question!");
        reply.setAuthor(owner);
        reply.setThread(thread);
    }

    // ── Forum CRUD ────────────────────────────────────────────────

    @Test
    void createForum_happyPath_savesForumAndOwnerMember() {
        when(forumRepo.existsBySlug("new-forum")).thenReturn(false);
        when(profileRepo.findByUsername("owner_user")).thenReturn(Optional.of(ownerProfile));
        when(forumRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(memberRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(threadRepo.countByForum(any())).thenReturn(0L);
        when(profileRepo.findByUser(owner)).thenReturn(Optional.of(ownerProfile));

        var req = new CreateForumRequest("New Forum", "new-forum", "A description", null);
        ForumSummaryResponse result = forumService.createForum(req, "owner_user");

        assertThat(result.name()).isEqualTo("New Forum");
        assertThat(result.slug()).isEqualTo("new-forum");
        assertThat(result.ownerUsername()).isEqualTo("owner_user");
        verify(forumRepo).save(any(Forum.class));
        verify(memberRepo).save(any(ForumMember.class));
    }

    @Test
    void createForum_duplicateSlug_throwsConflict() {
        when(forumRepo.existsBySlug("test-forum")).thenReturn(true);

        var req = new CreateForumRequest("Test", "test-forum", null, null);
        assertThatThrownBy(() -> forumService.createForum(req, "owner_user"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("test-forum");

        verify(forumRepo, never()).save(any());
    }

    @Test
    void getForums_blankQuery_returnsAllForumsPaginated() {
        Pageable pageable = PageRequest.of(0, 10);
        when(forumRepo.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(forum), pageable, 1));
        when(threadRepo.countByForum(forum)).thenReturn(3L);
        when(profileRepo.findByUser(owner)).thenReturn(Optional.of(ownerProfile));

        var result = forumService.getForums("", 0, 10);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).slug()).isEqualTo("test-forum");
        assertThat(result.getContent().get(0).threadCount()).isEqualTo(3L);
    }

    @Test
    void deleteForum_byOwner_deletesForum() {
        when(forumRepo.findBySlug("test-forum")).thenReturn(Optional.of(forum));
        when(profileRepo.findByUsername("owner_user")).thenReturn(Optional.of(ownerProfile));

        forumService.deleteForum("test-forum", "owner_user");

        verify(forumRepo).delete(forum);
    }

    @Test
    void deleteForum_byNonOwner_throwsForbidden() {
        when(forumRepo.findBySlug("test-forum")).thenReturn(Optional.of(forum));
        when(profileRepo.findByUsername("other_user")).thenReturn(Optional.of(otherProfile));

        assertThatThrownBy(() -> forumService.deleteForum("test-forum", "other_user"))
                .isInstanceOf(ForbiddenException.class);

        verify(forumRepo, never()).delete(any());
    }

    @Test
    void updateForum_byAdmin_updatesAndReturns() {
        when(forumRepo.findBySlug("test-forum")).thenReturn(Optional.of(forum));
        when(profileRepo.findByUsername("owner_user")).thenReturn(Optional.of(ownerProfile));
        when(forumRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(threadRepo.countByForum(forum)).thenReturn(0L);
        when(profileRepo.findByUser(owner)).thenReturn(Optional.of(ownerProfile));

        var req = new UpdateForumRequest("Updated Name", "New description", null);
        ForumSummaryResponse result = forumService.updateForum("test-forum", req, "owner_user");

        assertThat(result.name()).isEqualTo("Updated Name");
        verify(forumRepo).save(forum);
    }

    // ── Categories ────────────────────────────────────────────────

    @Test
    void createCategory_byAdmin_savesAndReturns() {
        when(forumRepo.findBySlug("test-forum")).thenReturn(Optional.of(forum));
        when(profileRepo.findByUsername("owner_user")).thenReturn(Optional.of(ownerProfile));
        when(categoryRepo.existsByNameIgnoreCaseAndForum("Backend", forum)).thenReturn(false);
        when(categoryRepo.save(any())).thenAnswer(inv -> {
            ForumCategory c = inv.getArgument(0);
            c.setPublicId(UUID.randomUUID());
            return c;
        });

        var req = new CreateForumCategoryRequest("Backend");
        ForumCategoryResponse result = forumService.createCategory("test-forum", req, "owner_user");

        assertThat(result.name()).isEqualTo("Backend");
        verify(categoryRepo).save(any(ForumCategory.class));
    }

    @Test
    void createCategory_duplicateName_throwsConflict() {
        when(forumRepo.findBySlug("test-forum")).thenReturn(Optional.of(forum));
        when(profileRepo.findByUsername("owner_user")).thenReturn(Optional.of(ownerProfile));
        when(categoryRepo.existsByNameIgnoreCaseAndForum("Tech", forum)).thenReturn(true);

        var req = new CreateForumCategoryRequest("Tech");
        assertThatThrownBy(() -> forumService.createCategory("test-forum", req, "owner_user"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Tech");

        verify(categoryRepo, never()).save(any());
    }

    @Test
    void createCategory_byNonAdmin_throwsForbidden() {
        when(forumRepo.findBySlug("test-forum")).thenReturn(Optional.of(forum));
        when(profileRepo.findByUsername("other_user")).thenReturn(Optional.of(otherProfile));
        when(memberRepo.findByForumAndUser(forum, otherUser)).thenReturn(Optional.empty());

        var req = new CreateForumCategoryRequest("Backend");
        assertThatThrownBy(() -> forumService.createCategory("test-forum", req, "other_user"))
                .isInstanceOf(ForbiddenException.class);

        verify(categoryRepo, never()).save(any());
    }

    @Test
    void deleteCategory_byAdmin_deletesCategory() {
        when(forumRepo.findBySlug("test-forum")).thenReturn(Optional.of(forum));
        when(profileRepo.findByUsername("owner_user")).thenReturn(Optional.of(ownerProfile));
        when(categoryRepo.findByPublicIdAndForum(category.getPublicId(), forum))
                .thenReturn(Optional.of(category));

        forumService.deleteCategory("test-forum", category.getPublicId(), "owner_user");

        verify(categoryRepo).delete(category);
    }

    // ── Members ───────────────────────────────────────────────────

    @Test
    void addMember_ownerAddsAdmin_savesWithAdminRole() {
        when(forumRepo.findBySlug("test-forum")).thenReturn(Optional.of(forum));
        when(profileRepo.findByUsername("owner_user")).thenReturn(Optional.of(ownerProfile));
        when(userRepo.findByPublicId(otherUser.getPublicId())).thenReturn(Optional.of(otherUser));
        when(memberRepo.findByForumAndUser(forum, otherUser)).thenReturn(Optional.empty());
        when(memberRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(profileRepo.findByUser(otherUser)).thenReturn(Optional.of(otherProfile));

        var req = new AddForumMemberRequest(otherUser.getPublicId(), ForumRole.ADMIN);
        ForumMemberResponse result = forumService.addMember("test-forum", req, "owner_user");

        assertThat(result.role()).isEqualTo(ForumRole.ADMIN);
        assertThat(result.username()).isEqualTo("other_user");
        verify(memberRepo).save(any(ForumMember.class));
    }

    @Test
    void addMember_nonOwnerTriesToAddAdmin_throwsForbidden() {
        when(forumRepo.findBySlug("test-forum")).thenReturn(Optional.of(forum));
        when(profileRepo.findByUsername("other_user")).thenReturn(Optional.of(otherProfile));

        var req = new AddForumMemberRequest(UUID.randomUUID(), ForumRole.ADMIN);
        assertThatThrownBy(() -> forumService.addMember("test-forum", req, "other_user"))
                .isInstanceOf(ForbiddenException.class);

        verify(memberRepo, never()).save(any());
    }

    @Test
    void addMember_adminAddsModerator_savesWithModRole() {
        ForumMember adminMember = new ForumMember();
        adminMember.setForum(forum);
        adminMember.setUser(otherUser);
        adminMember.setRole(ForumRole.ADMIN);

        User newUser = new User();
        newUser.setId(3L);
        newUser.setPublicId(UUID.randomUUID());
        newUser.setEmail("newmod@test.com");
        Profile newProfile = new Profile();
        newProfile.setUsername("new_mod");
        newProfile.setUser(newUser);

        when(forumRepo.findBySlug("test-forum")).thenReturn(Optional.of(forum));
        when(profileRepo.findByUsername("other_user")).thenReturn(Optional.of(otherProfile));
        when(memberRepo.findByForumAndUser(forum, otherUser)).thenReturn(Optional.of(adminMember));
        when(userRepo.findByPublicId(newUser.getPublicId())).thenReturn(Optional.of(newUser));
        when(memberRepo.findByForumAndUser(forum, newUser)).thenReturn(Optional.empty());
        when(memberRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(profileRepo.findByUser(newUser)).thenReturn(Optional.of(newProfile));

        var req = new AddForumMemberRequest(newUser.getPublicId(), ForumRole.MODERATOR);
        ForumMemberResponse result = forumService.addMember("test-forum", req, "other_user");

        assertThat(result.role()).isEqualTo(ForumRole.MODERATOR);
    }

    @Test
    void removeMember_byAdmin_removesSuccessfully() {
        ForumMember membership = new ForumMember();
        membership.setForum(forum);
        membership.setUser(otherUser);
        membership.setRole(ForumRole.MODERATOR);

        when(forumRepo.findBySlug("test-forum")).thenReturn(Optional.of(forum));
        when(profileRepo.findByUsername("owner_user")).thenReturn(Optional.of(ownerProfile));
        when(userRepo.findByPublicId(otherUser.getPublicId())).thenReturn(Optional.of(otherUser));
        when(memberRepo.findByForumAndUser(forum, otherUser)).thenReturn(Optional.of(membership));

        forumService.removeMember("test-forum", otherUser.getPublicId(), "owner_user");

        verify(memberRepo).delete(membership);
    }

    // ── Threads ───────────────────────────────────────────────────

    @Test
    void getThreads_allInForum_returnsPaginatedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        when(forumRepo.findBySlug("test-forum")).thenReturn(Optional.of(forum));
        when(threadRepo.findByForum(eq(forum), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(thread), pageable, 1));
        when(replyRepo.countByThreadPublicId(thread.getPublicId())).thenReturn(2);
        when(profileRepo.findByUser(owner)).thenReturn(Optional.of(ownerProfile));

        var result = forumService.getThreads("test-forum", null, 0, 10);

        assertThat(result.getTotalElements()).isEqualTo(1);
        ThreadSummaryResponse summary = result.getContent().get(0);
        assertThat(summary.title()).isEqualTo("Best practices");
        assertThat(summary.forumSlug()).isEqualTo("test-forum");
        assertThat(summary.replyCount()).isEqualTo(2L);
    }

    @Test
    void getThread_existingThread_incrementsViewCountAndReturnsDetail() {
        thread.setViewCount(5L);
        when(threadRepo.findByPublicId(thread.getPublicId())).thenReturn(Optional.of(thread));
        when(threadRepo.save(thread)).thenReturn(thread);
        when(replyRepo.findByThreadPublicIdOrderByCreatedAtAsc(thread.getPublicId()))
                .thenReturn(List.of(reply));
        when(profileRepo.findByUser(owner)).thenReturn(Optional.of(ownerProfile));

        ThreadDetailResponse result = forumService.getThread(thread.getPublicId(), "owner_user");

        assertThat(result.viewCount()).isEqualTo(6L);
        assertThat(result.replies()).hasSize(1);
        assertThat(result.forumSlug()).isEqualTo("test-forum");
        verify(threadRepo).save(thread);
    }

    @Test
    void getThread_notFound_throwsResourceNotFoundException() {
        when(threadRepo.findByPublicId(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> forumService.getThread(UUID.randomUUID(), "owner_user"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createThread_withNewTags_autoCreatesTagsAndSavesThread() {
        when(forumRepo.findBySlug("test-forum")).thenReturn(Optional.of(forum));
        when(profileRepo.findByUsername("owner_user")).thenReturn(Optional.of(ownerProfile));
        when(categoryRepo.findByPublicIdAndForum(category.getPublicId(), forum))
                .thenReturn(Optional.of(category));
        when(tagRepo.findByNameIgnoreCase("spring-boot")).thenReturn(Optional.empty());
        when(tagRepo.save(any(Tag.class))).thenAnswer(inv -> {
            Tag t = inv.getArgument(0);
            t.setPublicId(UUID.randomUUID());
            return t;
        });
        when(threadRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(profileRepo.findByUser(owner)).thenReturn(Optional.of(ownerProfile));

        var req = new CreateThreadRequest("Best practices",
                "What are the best practices for Spring Boot?",
                category.getPublicId(), List.of("spring-boot"));

        ThreadDetailResponse result = forumService.createThread("test-forum", req, "owner_user");

        assertThat(result.title()).isEqualTo("Best practices");
        assertThat(result.forumSlug()).isEqualTo("test-forum");
        verify(tagRepo).save(any(Tag.class));
        verify(threadRepo).save(any(ForumThread.class));
    }

    @Test
    void createThread_existingTag_reusesTagWithoutCreating() {
        Tag existingTag = new Tag();
        existingTag.setPublicId(UUID.randomUUID());
        existingTag.setName("spring-boot");

        when(forumRepo.findBySlug("test-forum")).thenReturn(Optional.of(forum));
        when(profileRepo.findByUsername("owner_user")).thenReturn(Optional.of(ownerProfile));
        when(categoryRepo.findByPublicIdAndForum(category.getPublicId(), forum))
                .thenReturn(Optional.of(category));
        when(tagRepo.findByNameIgnoreCase("spring-boot")).thenReturn(Optional.of(existingTag));
        when(threadRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(profileRepo.findByUser(owner)).thenReturn(Optional.of(ownerProfile));

        var req = new CreateThreadRequest("Best practices",
                "What are the best practices?", category.getPublicId(), List.of("spring-boot"));

        forumService.createThread("test-forum", req, "owner_user");

        verify(tagRepo, never()).save(any());
    }

    @Test
    void createThread_categoryNotInForum_throwsResourceNotFoundException() {
        when(forumRepo.findBySlug("test-forum")).thenReturn(Optional.of(forum));
        when(profileRepo.findByUsername("owner_user")).thenReturn(Optional.of(ownerProfile));
        when(categoryRepo.findByPublicIdAndForum(any(), eq(forum))).thenReturn(Optional.empty());

        var req = new CreateThreadRequest("Title", "Body content here",
                UUID.randomUUID(), List.of());

        assertThatThrownBy(() -> forumService.createThread("test-forum", req, "owner_user"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(threadRepo, never()).save(any());
    }

    @Test
    void deleteThread_byAuthor_deletesThread() {
        when(threadRepo.findByPublicId(thread.getPublicId())).thenReturn(Optional.of(thread));
        when(profileRepo.findByUsername("owner_user")).thenReturn(Optional.of(ownerProfile));

        forumService.deleteThread(thread.getPublicId(), "owner_user");

        verify(threadRepo).delete(thread);
    }

    @Test
    void deleteThread_byForumStaff_deletesThread() {
        ForumMember staffMember = new ForumMember();
        staffMember.setForum(forum);
        staffMember.setUser(otherUser);
        staffMember.setRole(ForumRole.MODERATOR);

        when(threadRepo.findByPublicId(thread.getPublicId())).thenReturn(Optional.of(thread));
        when(profileRepo.findByUsername("other_user")).thenReturn(Optional.of(otherProfile));
        when(memberRepo.existsByForumAndUser(forum, otherUser)).thenReturn(true);

        forumService.deleteThread(thread.getPublicId(), "other_user");

        verify(threadRepo).delete(thread);
    }

    @Test
    void deleteThread_byStranger_throwsForbidden() {
        when(threadRepo.findByPublicId(thread.getPublicId())).thenReturn(Optional.of(thread));
        when(profileRepo.findByUsername("other_user")).thenReturn(Optional.of(otherProfile));
        when(memberRepo.existsByForumAndUser(forum, otherUser)).thenReturn(false);

        assertThatThrownBy(() -> forumService.deleteThread(thread.getPublicId(), "other_user"))
                .isInstanceOf(ForbiddenException.class);

        verify(threadRepo, never()).delete(any());
    }

    // ── Replies ───────────────────────────────────────────────────

    @Test
    void createReply_noParent_savesTopLevelReply() {
        when(threadRepo.findByPublicId(thread.getPublicId())).thenReturn(Optional.of(thread));
        when(profileRepo.findByUsername("owner_user")).thenReturn(Optional.of(ownerProfile));
        when(replyRepo.save(any())).thenReturn(reply);
        when(profileRepo.findByUser(owner)).thenReturn(Optional.of(ownerProfile));

        var req = new CreateReplyRequest("Great question!", null);
        ReplyResponse result = forumService.createReply(thread.getPublicId(), req, "owner_user");

        assertThat(result.body()).isEqualTo("Great question!");
        assertThat(result.parentReplyPublicId()).isNull();
        verify(replyRepo).save(any(Reply.class));
    }

    @Test
    void createReply_withParent_setsParentReply() {
        Reply parent = new Reply();
        parent.setId(99L);
        parent.setPublicId(UUID.randomUUID());
        parent.setBody("Parent reply");
        parent.setAuthor(owner);
        parent.setThread(thread);

        when(threadRepo.findByPublicId(thread.getPublicId())).thenReturn(Optional.of(thread));
        when(profileRepo.findByUsername("owner_user")).thenReturn(Optional.of(ownerProfile));
        when(replyRepo.findByPublicId(parent.getPublicId())).thenReturn(Optional.of(parent));
        when(replyRepo.save(any())).thenAnswer(inv -> {
            Reply r = inv.getArgument(0);
            r.setPublicId(UUID.randomUUID());
            return r;
        });
        when(profileRepo.findByUser(owner)).thenReturn(Optional.of(ownerProfile));

        var req = new CreateReplyRequest("Nested reply body.", parent.getPublicId());
        ReplyResponse result = forumService.createReply(thread.getPublicId(), req, "owner_user");

        assertThat(result.parentReplyPublicId()).isEqualTo(parent.getPublicId());
    }

    @Test
    void createReply_parentNotFound_throwsResourceNotFoundException() {
        when(threadRepo.findByPublicId(thread.getPublicId())).thenReturn(Optional.of(thread));
        when(profileRepo.findByUsername("owner_user")).thenReturn(Optional.of(ownerProfile));
        when(replyRepo.findByPublicId(any())).thenReturn(Optional.empty());

        var req = new CreateReplyRequest("Some reply.", UUID.randomUUID());

        assertThatThrownBy(() -> forumService.createReply(thread.getPublicId(), req, "owner_user"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteReply_byAuthor_deletesReply() {
        when(replyRepo.findByPublicId(reply.getPublicId())).thenReturn(Optional.of(reply));
        when(profileRepo.findByUsername("owner_user")).thenReturn(Optional.of(ownerProfile));

        forumService.deleteReply(reply.getPublicId(), "owner_user");

        verify(replyRepo).delete(reply);
    }

    @Test
    void deleteReply_byForumStaff_deletesReply() {
        when(replyRepo.findByPublicId(reply.getPublicId())).thenReturn(Optional.of(reply));
        when(profileRepo.findByUsername("other_user")).thenReturn(Optional.of(otherProfile));
        when(memberRepo.existsByForumAndUser(forum, otherUser)).thenReturn(true);

        forumService.deleteReply(reply.getPublicId(), "other_user");

        verify(replyRepo).delete(reply);
    }

    @Test
    void deleteReply_byStranger_throwsForbidden() {
        when(replyRepo.findByPublicId(reply.getPublicId())).thenReturn(Optional.of(reply));
        when(profileRepo.findByUsername("other_user")).thenReturn(Optional.of(otherProfile));
        when(memberRepo.existsByForumAndUser(forum, otherUser)).thenReturn(false);

        assertThatThrownBy(() -> forumService.deleteReply(reply.getPublicId(), "other_user"))
                .isInstanceOf(ForbiddenException.class);

        verify(replyRepo, never()).delete(any());
    }

    @Test
    void deleteReply_notFound_throwsResourceNotFoundException() {
        when(replyRepo.findByPublicId(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> forumService.deleteReply(UUID.randomUUID(), "owner_user"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
