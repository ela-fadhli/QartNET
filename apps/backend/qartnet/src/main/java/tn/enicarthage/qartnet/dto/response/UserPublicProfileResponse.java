package tn.enicarthage.qartnet.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserPublicProfileResponse(
        UUID publicId,
        String username,
        String firstName,
        String lastName,
        String bio,
        String profilePictureUrl,
        List<String> skills,
        LocalDateTime createdAt
) {}
