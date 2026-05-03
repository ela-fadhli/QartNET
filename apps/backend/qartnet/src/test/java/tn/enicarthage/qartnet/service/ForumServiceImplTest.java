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
import tn.enicarthage.qartnet.dto.request.CreateReplyRequest;
import tn.enicarthage.qartnet.dto.request.CreateThreadRequest;
import tn.enicarthage.qartnet.dto.response.ReplyResponse;
import tn.enicarthage.qartnet.dto.response.ThreadDetailResponse;
import tn.enicarthage.qartnet.dto.response.ThreadSummaryResponse;
import tn.enicarthage.qartnet.model.*;
import tn.enicarthage.qartnet.repository.*;
import tn.enicarthage.qartnet.service.impl.ForumServiceImpl;
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

    @Mock CategoryRepository categoryRepository;
    @Mock TagRepository tagRepository;
    @Mock ForumThreadRepository threadRepository;
    @Mock ReplyRepository replyRepository;
    @Mock UserRepository userRepository;
    @Mock ProfileRepository profileRepository;

    @InjectMocks ForumServiceImpl forumService;

    private User author;
    private Profile profile;
    private Category category;
    private ForumThread thread;
    private Reply reply;

    @BeforeEach
    void setUp() {
        author = new User();
        author.setPublicId(UUID.randomUUID());

        profile = new Profile();
        profile.setUsername("yassine_dev");
        profile.setUser(author);

        category = new Category();
        category.setPublicId(UUID.randomUUID());
        category.setName("Tech");
        category.setDescription("Software engineering discussions");

        thread = new ForumThread();
        thread.setPublicId(UUID.randomUUID());
        thread.setTitle("Spring Boot 3 best practices");
        thread.setBody("What are the best practices for structuring a Spring Boot 3 project?");
        thread.setAuthor(author);
        thread.setCategory(category);

        reply = new Reply();
        reply.setPublicId(UUID.randomUUID());
        reply.setBody("Use layered architecture with clear separation of concerns.");
        reply.setAuthor(author);
        reply.setThread(thread);
    }

    // --- getCategories ---

    @Test
    void getCategories_returnsAllMapped() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        var result = forumService.getCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Tech");
        assertThat(result.get(0).publicId()).isEqualTo(category.getPublicId());
    }

    // --- getTags ---

    @Test
    void getTags_returnsAllMapped() {
        Tag tag = new Tag();
        tag.setPublicId(UUID.randomUUID());
        tag.setName("spring-boot");
        when(tagRepository.findAll()).thenReturn(List.of(tag));

        var result = forumService.getTags();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("spring-boot");
    }

    // --- getThreads ---

    @Test
    void getThreads_noFilters_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(threadRepository.findFiltered(null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(thread)));
        when(profileRepository.findByUser(author)).thenReturn(Optional.of(profile));
        when(replyRepository.countByThreadPublicId(thread.getPublicId())).thenReturn(3);

        var result = forumService.getThreads(null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        ThreadSummaryResponse summary = result.getContent().get(0);
        assertThat(summary.title()).isEqualTo("Spring Boot 3 best practices");
        assertThat(summary.authorUsername()).isEqualTo("yassine_dev");
        assertThat(summary.replyCount()).isEqualTo(3);
    }

    // --- getThread ---

    @Test
    void getThread_existingThread_incrementsViewCountAndReturnsDetail() {
        thread.setViewCount(5);
        when(threadRepository.findByPublicId(thread.getPublicId())).thenReturn(Optional.of(thread));
        when(threadRepository.save(thread)).thenReturn(thread);
        when(replyRepository.findByThreadPublicIdOrderByCreatedAtAsc(thread.getPublicId()))
                .thenReturn(List.of(reply));
        when(profileRepository.findByUser(author)).thenReturn(Optional.of(profile));

        ThreadDetailResponse result = forumService.getThread(thread.getPublicId());

        assertThat(result.viewCount()).isEqualTo(6);
        assertThat(result.title()).isEqualTo("Spring Boot 3 best practices");
        assertThat(result.replies()).hasSize(1);
        verify(threadRepository).save(thread);
    }

    @Test
    void getThread_notFound_throwsResourceNotFoundException() {
        when(threadRepository.findByPublicId(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> forumService.getThread(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- createThread ---

    @Test
    void createThread_validRequest_savesAndReturnsSummary() {
        when(userRepository.findByPublicId(author.getPublicId())).thenReturn(Optional.of(author));
        when(categoryRepository.findByPublicId(category.getPublicId())).thenReturn(Optional.of(category));
        when(tagRepository.findByPublicIdIn(List.of())).thenReturn(List.of());
        when(threadRepository.save(any())).thenReturn(thread);
        when(profileRepository.findByUser(author)).thenReturn(Optional.of(profile));
        when(replyRepository.countByThreadPublicId(any())).thenReturn(0);

        var request = new CreateThreadRequest(
                "Spring Boot 3 best practices",
                "What are the best practices for structuring a Spring Boot 3 project?",
                category.getPublicId(),
                List.of()
        );

        ThreadSummaryResponse result = forumService.createThread(author.getPublicId(), request);

        assertThat(result.title()).isEqualTo("Spring Boot 3 best practices");
        assertThat(result.authorUsername()).isEqualTo("yassine_dev");
        verify(threadRepository).save(any(ForumThread.class));
    }

    @Test
    void createThread_categoryNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByPublicId(author.getPublicId())).thenReturn(Optional.of(author));
        when(categoryRepository.findByPublicId(any())).thenReturn(Optional.empty());

        var request = new CreateThreadRequest(
                "Some title here",
                "Some body content here",
                UUID.randomUUID(),
                List.of()
        );

        assertThatThrownBy(() -> forumService.createThread(author.getPublicId(), request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(threadRepository, never()).save(any());
    }

    @Test
    void createThread_authorNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByPublicId(any())).thenReturn(Optional.empty());

        var request = new CreateThreadRequest(
                "Some title here",
                "Some body content here",
                category.getPublicId(),
                List.of()
        );

        assertThatThrownBy(() -> forumService.createThread(UUID.randomUUID(), request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- createReply ---

    @Test
    void createReply_noParent_savesAndReturnsResponse() {
        when(userRepository.findByPublicId(author.getPublicId())).thenReturn(Optional.of(author));
        when(threadRepository.findByPublicId(thread.getPublicId())).thenReturn(Optional.of(thread));
        when(replyRepository.save(any())).thenReturn(reply);
        when(profileRepository.findByUser(author)).thenReturn(Optional.of(profile));

        var request = new CreateReplyRequest(
                "Use layered architecture with clear separation of concerns.", null);

        ReplyResponse result = forumService.createReply(author.getPublicId(), thread.getPublicId(), request);

        assertThat(result.body()).isEqualTo("Use layered architecture with clear separation of concerns.");
        assertThat(result.parentReplyPublicId()).isNull();
        verify(replyRepository).save(any(Reply.class));
    }

    @Test
    void createReply_withParent_setsParentReply() {
        Reply parent = new Reply();
        parent.setPublicId(UUID.randomUUID());
        parent.setBody("Parent reply");
        parent.setAuthor(author);
        parent.setThread(thread);

        when(userRepository.findByPublicId(author.getPublicId())).thenReturn(Optional.of(author));
        when(threadRepository.findByPublicId(thread.getPublicId())).thenReturn(Optional.of(thread));
        when(replyRepository.findByPublicId(parent.getPublicId())).thenReturn(Optional.of(parent));
        when(replyRepository.save(any())).thenAnswer(inv -> {
            Reply r = inv.getArgument(0);
            r.setPublicId(UUID.randomUUID());
            return r;
        });
        when(profileRepository.findByUser(author)).thenReturn(Optional.of(profile));

        var request = new CreateReplyRequest("Nested reply body.", parent.getPublicId());

        ReplyResponse result = forumService.createReply(author.getPublicId(), thread.getPublicId(), request);

        assertThat(result.parentReplyPublicId()).isEqualTo(parent.getPublicId());
    }

    @Test
    void createReply_parentNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByPublicId(author.getPublicId())).thenReturn(Optional.of(author));
        when(threadRepository.findByPublicId(thread.getPublicId())).thenReturn(Optional.of(thread));
        when(replyRepository.findByPublicId(any())).thenReturn(Optional.empty());

        var request = new CreateReplyRequest("Some reply.", UUID.randomUUID());

        assertThatThrownBy(() -> forumService.createReply(author.getPublicId(), thread.getPublicId(), request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- deleteReply ---

    @Test
    void deleteReply_callerIsOwner_deletesReply() {
        when(replyRepository.findByPublicId(reply.getPublicId())).thenReturn(Optional.of(reply));

        forumService.deleteReply(author.getPublicId(), reply.getPublicId());

        verify(replyRepository).delete(reply);
    }

    @Test
    void deleteReply_callerIsNotOwner_throwsForbiddenException() {
        when(replyRepository.findByPublicId(reply.getPublicId())).thenReturn(Optional.of(reply));

        assertThatThrownBy(() -> forumService.deleteReply(UUID.randomUUID(), reply.getPublicId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("own replies");

        verify(replyRepository, never()).delete(any());
    }

    @Test
    void deleteReply_replyNotFound_throwsResourceNotFoundException() {
        when(replyRepository.findByPublicId(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> forumService.deleteReply(author.getPublicId(), UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
