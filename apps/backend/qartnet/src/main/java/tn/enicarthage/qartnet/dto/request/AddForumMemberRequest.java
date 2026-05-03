package tn.enicarthage.qartnet.dto.request;

import jakarta.validation.constraints.NotNull;
import tn.enicarthage.qartnet.shared.enums.ForumRole;

import java.util.UUID;

public record AddForumMemberRequest(
        @NotNull UUID userPublicId,
        @NotNull ForumRole role
) {}
