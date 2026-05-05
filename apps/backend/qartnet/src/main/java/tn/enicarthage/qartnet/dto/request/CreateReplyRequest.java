package tn.enicarthage.qartnet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateReplyRequest(
        @NotBlank @Size(min = 1, max = 2000) String body,
        UUID parentReplyPublicId
) {}
