package tn.enicarthage.qartnet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.qartnet.dto.request.*;
import tn.enicarthage.qartnet.dto.response.*;
import tn.enicarthage.qartnet.model.*;
import tn.enicarthage.qartnet.repository.*;
import tn.enicarthage.qartnet.service.ForumService;
import tn.enicarthage.qartnet.shared.enums.ForumRole;
import tn.enicarthage.qartnet.shared.exception.ConflictException;
import tn.enicarthage.qartnet.shared.exception.ForbiddenException;
import tn.enicarthage.qartnet.shared.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForumServiceImpl implements ForumService {

    private final ForumRepository forumRepo;
    private final ForumMemberRepository memberRepo;
    private final ForumCategoryRepository categoryRepo;
    private final ForumThreadRepository threadRepo;
    private final ReplyRepository replyRepo;
    private final UserRepository userRepo;
    private final ProfileRepository profileRepo;
    private final TagRepository tagRepo;

    // ── Forum CRUD ──────────────────────────────────────────────

    @Override
    @Transactional
    public ForumSummaryResponse createForum(CreateForumRequest req, String username) {
        if (forumRepo.existsBySlug(req.slug()))
            throw new ConflictException("Slug already taken: " + req.slug());

        User owner = requireUser(username);

        Forum forum = new Forum();
        forum.setName(req.name());
        forum.setSlug(req.slug());
        forum.setDescription(req.description());
        forum.setBanner(req.banner());
        forum.setOwner(owner);
        forumRepo.save(forum);

        ForumMember ownerMember = new ForumMember();
        ownerMember.setForum(forum);
        ownerMember.setUser(owner);
        ownerMember.setRole(ForumRole.ADMIN);
        memberRepo.save(ownerMember);

        return toSummary(forum);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ForumSummaryResponse> getForums(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Forum> forums = query.isBlank()
                ? forumRepo.findAll(pageable)
                : forumRepo.findByNameContainingIgnoreCase(query, pageable);
        return forums.map(this::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public ForumDetailResponse getForum(String slug, String username) {
        Forum forum = requireForum(slug);
        User user = requireUser(username);
        boolean owner = isOwner(forum, user);
        boolean isAdmin = owner || memberRepo.findByForumAndUser(forum, user)
                .map(m -> m.getRole() == ForumRole.ADMIN).orElse(false);
        boolean isMod = !isAdmin && memberRepo.findByForumAndUser(forum, user)
                .map(m -> m.getRole() == ForumRole.MODERATOR).orElse(false);
        List<ForumCategoryResponse> cats = categoryRepo.findByForum(forum).stream()
                .map(c -> new ForumCategoryResponse(c.getPublicId(), c.getName())).toList();
        long threadCount = threadRepo.countByForum(forum);
        return new ForumDetailResponse(
                forum.getPublicId(), forum.getName(), forum.getSlug(),
                forum.getDescription(), forum.getBanner(),
                usernameOf(forum.getOwner()), cats,
                threadCount, forum.getCreatedAt(), isAdmin, isMod, owner
        );
    }

    @Override
    @Transactional
    public ForumSummaryResponse updateForum(String slug, UpdateForumRequest req, String username) {
        Forum forum = requireForum(slug);
        requireAdminOrOwner(forum, username);
        if (req.name() != null)        forum.setName(req.name());
        if (req.description() != null) forum.setDescription(req.description());
        if (req.banner() != null)      forum.setBanner(req.banner());
        forumRepo.save(forum);
        return toSummary(forum);
    }

    @Override
    @Transactional
    public void deleteForum(String slug, String username) {
        Forum forum = requireForum(slug);
        requireOwner(forum, username);
        forumRepo.delete(forum);
    }

    // ── Categories ───────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ForumCategoryResponse> getCategories(String slug) {
        Forum forum = requireForum(slug);
        return categoryRepo.findByForum(forum).stream()
                .map(c -> new ForumCategoryResponse(c.getPublicId(), c.getName()))
                .toList();
    }

    @Override
    @Transactional
    public ForumCategoryResponse createCategory(String slug, CreateForumCategoryRequest req, String username) {
        Forum forum = requireForum(slug);
        requireAdminOrOwner(forum, username);
        if (categoryRepo.existsByNameIgnoreCaseAndForum(req.name(), forum))
            throw new ConflictException("Category '" + req.name() + "' already exists in this forum");
        ForumCategory cat = new ForumCategory();
        cat.setName(req.name());
        cat.setForum(forum);
        categoryRepo.save(cat);
        return new ForumCategoryResponse(cat.getPublicId(), cat.getName());
    }

    @Override
    @Transactional
    public void deleteCategory(String slug, UUID categoryPublicId, String username) {
        Forum forum = requireForum(slug);
        requireAdminOrOwner(forum, username);
        ForumCategory cat = categoryRepo.findByPublicIdAndForum(categoryPublicId, forum)
                .orElseThrow(() -> ResourceNotFoundException.of("Category", categoryPublicId));
        categoryRepo.delete(cat);
    }

    // ── Members ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ForumMemberResponse> getMembers(String slug) {
        Forum forum = requireForum(slug);
        return memberRepo.findByForum(forum).stream()
                .map(m -> new ForumMemberResponse(
                        m.getUser().getPublicId(), usernameOf(m.getUser()), m.getRole()))
                .toList();
    }

    @Override
    @Transactional
    public ForumMemberResponse addMember(String slug, AddForumMemberRequest req, String username) {
        Forum forum = requireForum(slug);
        if (req.role() == ForumRole.ADMIN) requireOwner(forum, username);
        else requireAdminOrOwner(forum, username);

        User target = userRepo.findByPublicId(req.userPublicId())
                .orElseThrow(() -> ResourceNotFoundException.of("User", req.userPublicId()));
        if (isOwner(forum, target))
            throw new ConflictException("Cannot assign a role to the forum owner");

        ForumMember member = memberRepo.findByForumAndUser(forum, target)
                .orElseGet(() -> { ForumMember m = new ForumMember(); m.setForum(forum); m.setUser(target); return m; });
        member.setRole(req.role());
        memberRepo.save(member);
        return new ForumMemberResponse(target.getPublicId(), usernameOf(target), req.role());
    }

    @Override
    @Transactional
    public void removeMember(String slug, UUID userPublicId, String username) {
        Forum forum = requireForum(slug);
        requireAdminOrOwner(forum, username);
        User target = userRepo.findByPublicId(userPublicId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userPublicId));
        ForumMember member = memberRepo.findByForumAndUser(forum, target)
                .orElseThrow(() -> ResourceNotFoundException.of("Member", userPublicId));
        memberRepo.delete(member);
    }

    // ── Threads ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<ThreadSummaryResponse> getThreads(String slug, UUID categoryPublicId, int page, int size) {
        Forum forum = requireForum(slug);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ForumThread> threads = categoryPublicId != null
                ? categoryRepo.findByPublicIdAndForum(categoryPublicId, forum)
                  .map(cat -> threadRepo.findByForumAndCategory(forum, cat, pageable))
                  .orElseThrow(() -> ResourceNotFoundException.of("Category", categoryPublicId))
                : threadRepo.findByForum(forum, pageable);
        return threads.map(this::toSummary);
    }

    @Override
    @Transactional
    public ThreadDetailResponse getThread(UUID publicId, String username) {
        ForumThread thread = requireThread(publicId);
        thread.setViewCount(thread.getViewCount() + 1);
        threadRepo.save(thread);
        List<ReplyResponse> replies = replyRepo
                .findByThreadPublicIdOrderByCreatedAtAsc(publicId)
                .stream().map(this::toReplyResponse).toList();
        return toDetail(thread, replies);
    }

    @Override
    @Transactional
    public ThreadDetailResponse createThread(String slug, CreateThreadRequest req, String username) {
        Forum forum = requireForum(slug);
        User author = requireUser(username);
        ForumCategory category = categoryRepo.findByPublicIdAndForum(req.categoryPublicId(), forum)
                .orElseThrow(() -> ResourceNotFoundException.of("Category", req.categoryPublicId()));
        List<Tag> tags = resolveOrCreateTags(req.tagNames());

        ForumThread thread = new ForumThread();
        thread.setForum(forum);
        thread.setCategory(category);
        thread.setTitle(req.title());
        thread.setBody(req.body());
        thread.setAuthor(author);
        thread.getTags().addAll(tags);
        threadRepo.save(thread);
        return toDetail(thread, List.of());
    }

    @Override
    @Transactional
    public void deleteThread(UUID publicId, String username) {
        ForumThread thread = requireThread(publicId);
        User user = requireUser(username);
        boolean isAuthor = thread.getAuthor().getId().equals(user.getId());
        boolean isStaff = isOwner(thread.getForum(), user) || memberRepo.existsByForumAndUser(thread.getForum(), user);
        if (!isAuthor && !isStaff) throw new ForbiddenException("Cannot delete this thread");
        threadRepo.delete(thread);
    }

    // ── Replies ───────────────────────────────────────────────────

    @Override
    @Transactional
    public ReplyResponse createReply(UUID threadPublicId, CreateReplyRequest req, String username) {
        ForumThread thread = requireThread(threadPublicId);
        User author = requireUser(username);
        Reply parent = null;
        if (req.parentReplyPublicId() != null)
            parent = replyRepo.findByPublicId(req.parentReplyPublicId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Reply", req.parentReplyPublicId()));
        Reply reply = new Reply();
        reply.setBody(req.body());
        reply.setAuthor(author);
        reply.setThread(thread);
        reply.setParentReply(parent);
        replyRepo.save(reply);
        return toReplyResponse(reply);
    }

    @Override
    @Transactional
    public void deleteReply(UUID replyPublicId, String username) {
        Reply reply = replyRepo.findByPublicId(replyPublicId)
                .orElseThrow(() -> ResourceNotFoundException.of("Reply", replyPublicId));
        User user = requireUser(username);
        boolean isAuthor = reply.getAuthor().getId().equals(user.getId());
        boolean isStaff = isOwner(reply.getThread().getForum(), user)
                || memberRepo.existsByForumAndUser(reply.getThread().getForum(), user);
        if (!isAuthor && !isStaff) throw new ForbiddenException("Cannot delete this reply");
        replyRepo.delete(reply);
    }

    @Override
    @Transactional
    public ThreadDetailResponse updateThread(UUID publicId, UpdateThreadRequest req, String username) {
        ForumThread thread = requireThread(publicId);
        User user = requireUser(username);
        if (!thread.getAuthor().getId().equals(user.getId()))
            throw new ForbiddenException("Cannot edit this thread");
        if (req.title() != null) thread.setTitle(req.title());
        if (req.body()  != null) thread.setBody(req.body());
        threadRepo.save(thread);
        List<ReplyResponse> replies = replyRepo
                .findByThreadPublicIdOrderByCreatedAtAsc(publicId)
                .stream().map(this::toReplyResponse).toList();
        return toDetail(thread, replies);
    }

    @Override
    @Transactional
    public ReplyResponse updateReply(UUID publicId, UpdateReplyRequest req, String username) {
        Reply reply = replyRepo.findByPublicId(publicId)
                .orElseThrow(() -> ResourceNotFoundException.of("Reply", publicId));
        User user = requireUser(username);
        if (!reply.getAuthor().getId().equals(user.getId()))
            throw new ForbiddenException("Cannot edit this reply");
        reply.setBody(req.body());
        replyRepo.save(reply);
        return toReplyResponse(reply);
    }

    // ── Private helpers ───────────────────────────────────────────

    private Forum requireForum(String slug) {
        return forumRepo.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Forum not found: " + slug));
    }

    private ForumThread requireThread(UUID publicId) {
        return threadRepo.findByPublicId(publicId)
                .orElseThrow(() -> ResourceNotFoundException.of("Thread", publicId));
    }

    private User requireUser(String publicIdStr) {
        return userRepo.findByPublicId(UUID.fromString(publicIdStr))
                .orElseThrow(() -> ResourceNotFoundException.of("User", publicIdStr));
    }

    private boolean isOwner(Forum forum, User user) {
        return forum.getOwner().getId().equals(user.getId());
    }

    private void requireOwner(Forum forum, String username) {
        User user = requireUser(username);
        if (!isOwner(forum, user))
            throw new ForbiddenException("Only the forum owner can perform this action");
    }

    private boolean isAdminOrOwner(Forum forum, User user) {
        return isOwner(forum, user) || memberRepo.findByForumAndUser(forum, user)
                .map(m -> m.getRole() == ForumRole.ADMIN).orElse(false);
    }

    private void requireAdminOrOwner(Forum forum, String username) {
        User user = requireUser(username);
        if (!isAdminOrOwner(forum, user))
            throw new ForbiddenException("Only forum admins can perform this action");
    }

    private List<Tag> resolveOrCreateTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return List.of();
        return tagNames.stream()
                .map(name -> tagRepo.findByNameIgnoreCase(name.trim())
                        .orElseGet(() -> { Tag t = new Tag(); t.setName(name.trim().toLowerCase()); return tagRepo.save(t); }))
                .toList();
    }

    private String usernameOf(User user) {
        return profileRepo.findByUser(user).map(Profile::getUsername).orElse("unknown");
    }

    private ForumSummaryResponse toSummary(Forum f) {
        long threadCount = threadRepo.countByForum(f);
        return new ForumSummaryResponse(
                f.getPublicId(), f.getName(), f.getSlug(), f.getDescription(),
                f.getBanner(), usernameOf(f.getOwner()), threadCount, f.getCreatedAt()
        );
    }

    private ThreadSummaryResponse toSummary(ForumThread t) {
        long replyCount = replyRepo.countByThreadPublicId(t.getPublicId());
        return new ThreadSummaryResponse(
                t.getPublicId(), t.getTitle(), usernameOf(t.getAuthor()),
                t.getForum().getSlug(), t.getForum().getName(),
                new ForumCategoryResponse(t.getCategory().getPublicId(), t.getCategory().getName()),
                t.getTags().stream().map(tag -> new TagResponse(tag.getPublicId(), tag.getName())).toList(),
                t.getViewCount(), replyCount, t.getCreatedAt()
        );
    }

    private ThreadDetailResponse toDetail(ForumThread t, List<ReplyResponse> replies) {
        return new ThreadDetailResponse(
                t.getPublicId(), t.getTitle(), t.getBody(), usernameOf(t.getAuthor()),
                t.getForum().getSlug(), t.getForum().getName(),
                new ForumCategoryResponse(t.getCategory().getPublicId(), t.getCategory().getName()),
                t.getTags().stream().map(tag -> new TagResponse(tag.getPublicId(), tag.getName())).toList(),
                t.getViewCount(), replies, t.getCreatedAt()
        );
    }

    private ReplyResponse toReplyResponse(Reply r) {
        return new ReplyResponse(
                r.getPublicId(), r.getBody(), usernameOf(r.getAuthor()),
                r.getParentReply() != null ? r.getParentReply().getPublicId() : null,
                r.getCreatedAt()
        );
    }
}