package tn.enicarthage.qartnet.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateThreadRequest(
        @Size(min = 5, max = 200) String title,
        @Size(min = 10, max = 10000) String body
) {}
