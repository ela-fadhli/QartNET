package tn.enicarthage.qartnet.dto.response;

import java.util.UUID;

public record CategoryResponse(
        UUID publicId,
        String name,
        String description
) {}
