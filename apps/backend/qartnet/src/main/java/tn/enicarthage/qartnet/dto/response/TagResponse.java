package tn.enicarthage.qartnet.dto.response;

import java.util.UUID;

public record TagResponse(
        UUID publicId,
        String name
) {} 