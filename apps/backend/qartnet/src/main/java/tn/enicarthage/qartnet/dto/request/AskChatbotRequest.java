package tn.enicarthage.qartnet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AskChatbotRequest(
        @NotBlank @Size(min = 1, max = 4000) String message
) {}
