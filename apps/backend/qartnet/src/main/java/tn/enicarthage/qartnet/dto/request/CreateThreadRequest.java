package tn.enicarthage.qartnet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record CreateThreadRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
        String title,

        @NotBlank(message = "Body is required")
        @Size(min = 10, message = "Body must be at least 10 characters")
        String body,

        @NotNull(message = "Category is required")
        UUID categoryPublicId,

        List<UUID> tagPublicIds
) {}