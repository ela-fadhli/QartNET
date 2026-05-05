package tn.enicarthage.qartnet.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ThreadDetailResponse(
        UUID publicId, String title, String body, String authorUsername,
        String forumSlug, String forumName,
        ForumCategoryResponse category, List<TagResponse> tags,
        long viewCount, List<ReplyResponse> replies, LocalDateTime createdAt
) {}
