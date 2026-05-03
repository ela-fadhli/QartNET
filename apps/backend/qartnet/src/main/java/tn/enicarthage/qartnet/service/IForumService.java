package tn.enicarthage.qartnet.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.enicarthage.qartnet.dto.request.CreateReplyRequest;
import tn.enicarthage.qartnet.dto.request.CreateThreadRequest;
import tn.enicarthage.qartnet.dto.response.*;
import java.util.List;
import java.util.UUID;

public interface IForumService {
    List<CategoryResponse> getCategories();
    List<TagResponse> getTags();
    Page<ThreadSummaryResponse> getThreads(UUID categoryPublicId, UUID tagPublicId, Pageable pageable);
    ThreadDetailResponse getThread(UUID publicId);
    ThreadSummaryResponse createThread(UUID authorPublicId, CreateThreadRequest request);
    ReplyResponse createReply(UUID authorPublicId, UUID threadPublicId, CreateReplyRequest request);
    void deleteReply(UUID callerPublicId, UUID replyPublicId);
}