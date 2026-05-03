package tn.enicarthage.qartnet.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ForumDetailResponse(
        UUID publicId, String name, String slug, String description, String banner,
        String ownerUsername, List<ForumCategoryResponse> categories,
        long threadCount, LocalDateTime createdAt,
        boolean isAdmin, boolean isModerator// current-user context flags
) {}
