package tn.enicarthage.qartnet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateReplyRequest(
        @NotBlank @Size(max = 2000) String body
) {}
