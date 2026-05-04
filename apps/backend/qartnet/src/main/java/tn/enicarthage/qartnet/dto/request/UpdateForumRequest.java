package tn.enicarthage.qartnet.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateForumRequest(
        @Size(min = 3, max = 100) String name,
        @Size(max = 500) String description,
        String banner
) {}