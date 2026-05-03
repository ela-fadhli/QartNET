package tn.enicarthage.qartnet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.qartnet.dto.request.CreateReplyRequest;
import tn.enicarthage.qartnet.dto.request.CreateThreadRequest;
import tn.enicarthage.qartnet.dto.response.*;
import tn.enicarthage.qartnet.model.*;
import tn.enicarthage.qartnet.repository.*;
import tn.enicarthage.qartnet.service.IForumService;
import tn.enicarthage.qartnet.shared.exception.ForbiddenException;
import tn.enicarthage.qartnet.shared.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForumServiceImpl implements IForumService {

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ForumThreadRepository threadRepository;
    private final ReplyRepository replyRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(c.getPublicId(), c.getName(), c.getDescription()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getTags() {
        return tagRepository.findAll().stream()
                .map(t -> new TagResponse(t.getPublicId(), t.getName()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ThreadSummaryResponse> getThreads(UUID categoryPublicId, UUID tagPublicId, Pageable pageable) {
        return threadRepository.findFiltered(categoryPublicId, tagPublicId, pageable)
                .map(this::toSummary);
    }

    @Override
    @Transactional
    public ThreadDetailResponse getThread(UUID publicId) {
        ForumThread thread = findThread(publicId);
        thread.setViewCount(thread.getViewCount() + 1);
        threadRepository.save(thread);

        List<ReplyResponse> replies = replyRepository
                .findByThreadPublicIdOrderByCreatedAtAsc(publicId)
                .stream()
                .map(this::toReplyResponse)
                .toList();

        return new ThreadDetailResponse(
                thread.getPublicId(),
                thread.getTitle(),
                thread.getBody(),
                usernameOf(thread.getAuthor()),
                new CategoryResponse(thread.getCategory().getPublicId(), thread.getCategory().getName(), thread.getCategory().getDescription()),
                thread.getTags().stream().map(t -> new TagResponse(t.getPublicId(), t.getName())).toList(),
                thread.getViewCount(),
                replies,
                thread.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public ThreadSummaryResponse createThread(UUID authorPublicId, CreateThreadRequest request) {
        User author = findUser(authorPublicId);
        Category category = categoryRepository.findByPublicId(request.categoryPublicId())
                .orElseThrow(() -> ResourceNotFoundException.of("Category", request.categoryPublicId()));

        List<UUID> tagIds = request.tagPublicIds() != null ? request.tagPublicIds() : List.of();
        List<Tag> tags = tagRepository.findByPublicIdIn(tagIds);

        ForumThread thread = new ForumThread();
        thread.setTitle(request.title());
        thread.setBody(request.body());
        thread.setAuthor(author);
        thread.setCategory(category);
        thread.getTags().addAll(tags);

        threadRepository.save(thread);
        return toSummary(thread);
    }

    @Override
    @Transactional
    public ReplyResponse createReply(UUID authorPublicId, UUID threadPublicId, CreateReplyRequest request) {
        User author = findUser(authorPublicId);
        ForumThread thread = findThread(threadPublicId);

        Reply parentReply = null;
        if (request.parentReplyPublicId() != null) {
            parentReply = replyRepository.findByPublicId(request.parentReplyPublicId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Reply", request.parentReplyPublicId()));
        }

        Reply reply = new Reply();
        reply.setBody(request.body());
        reply.setAuthor(author);
        reply.setThread(thread);
        reply.setParentReply(parentReply);

        replyRepository.save(reply);
        return toReplyResponse(reply);
    }

    @Override
    @Transactional
    public void deleteReply(UUID callerPublicId, UUID replyPublicId) {
        Reply reply = replyRepository.findByPublicId(replyPublicId)
                .orElseThrow(() -> ResourceNotFoundException.of("Reply", replyPublicId));

        if (!reply.getAuthor().getPublicId().equals(callerPublicId)) {
            throw new ForbiddenException("You can only delete your own replies");
        }

        replyRepository.delete(reply);
    }

    // --- private helpers ---

    private ForumThread findThread(UUID publicId) {
        return threadRepository.findByPublicId(publicId)
                .orElseThrow(() -> ResourceNotFoundException.of("Thread", publicId));
    }

    private User findUser(UUID publicId) {
        return userRepository.findByPublicId(publicId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", publicId));
    }

    private String usernameOf(User user) {
        return profileRepository.findByUser(user)
                .map(Profile::getUsername)
                .orElse("unknown");
    }

    private ThreadSummaryResponse toSummary(ForumThread t) {
        return new ThreadSummaryResponse(
                t.getPublicId(),
                t.getTitle(),
                usernameOf(t.getAuthor()),
                new CategoryResponse(t.getCategory().getPublicId(), t.getCategory().getName(), t.getCategory().getDescription()),
                t.getTags().stream().map(tag -> new TagResponse(tag.getPublicId(), tag.getName())).toList(),
                t.getViewCount(),
                replyRepository.countByThreadPublicId(t.getPublicId()),
                t.getCreatedAt()
        );
    }

    private ReplyResponse toReplyResponse(Reply r) {
        return new ReplyResponse(
                r.getPublicId(),
                r.getBody(),
                usernameOf(r.getAuthor()),
                r.getParentReply() != null ? r.getParentReply().getPublicId() : null,
                r.getCreatedAt()
        );
    }
}