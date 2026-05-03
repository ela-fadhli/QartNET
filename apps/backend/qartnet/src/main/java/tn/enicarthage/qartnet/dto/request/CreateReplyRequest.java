package tn.enicarthage.qartnet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateReplyRequest(
        @NotBlank(message = "Body is required")
        @Size(min = 1, max = 2000, message = "Reply must be between 1 and 2000 characters")
        String body,

        UUID parentReplyPublicId
) {}