package tn.enicarthage.qartnet.service;

import org.springframework.data.domain.Page;
import tn.enicarthage.qartnet.dto.request.*;
import tn.enicarthage.qartnet.dto.response.*;
import java.util.List;
import java.util.UUID;

public interface ForumService {
    // Forum CRUD
    ForumSummaryResponse createForum(CreateForumRequest req, String username);
    Page<ForumSummaryResponse> getForums(String query, int page, int size);
    ForumDetailResponse getForum(String slug, String username);
    ForumSummaryResponse updateForum(String slug, UpdateForumRequest req, String username);
    void deleteForum(String slug, String username);
    List<ForumCategoryResponse> getCategories(String slug);
    // Categories
    ForumCategoryResponse createCategory(String slug, CreateForumCategoryRequest req, String username);
    void deleteCategory(String slug, UUID categoryPublicId, String username);

    // Members
    ForumMemberResponse addMember(String slug, AddForumMemberRequest req, String username);
    void removeMember(String slug, UUID userPublicId, String username);
    List<ForumMemberResponse> getMembers(String slug);

    // Threads
    Page<ThreadSummaryResponse> getThreads(String slug, UUID categoryPublicId, int page, int size);
    ThreadDetailResponse getThread(UUID publicId, String username);
    ThreadDetailResponse createThread(String slug, CreateThreadRequest req, String username);
    void deleteThread(UUID publicId, String username);

    // Replies
    ReplyResponse createReply(UUID threadPublicId, CreateReplyRequest req, String username);
    void deleteReply(UUID replyPublicId, String username);

    // Edit
    ThreadDetailResponse updateThread(UUID publicId, UpdateThreadRequest req, String username);
    ReplyResponse updateReply(UUID publicId, UpdateReplyRequest req, String username);
}