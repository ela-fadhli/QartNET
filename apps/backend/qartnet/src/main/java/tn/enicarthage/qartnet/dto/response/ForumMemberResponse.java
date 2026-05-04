package tn.enicarthage.qartnet.dto.response;

import tn.enicarthage.qartnet.shared.enums.ForumRole;

import java.util.UUID;

public record ForumMemberResponse(UUID userPublicId, String username, ForumRole role) {}
