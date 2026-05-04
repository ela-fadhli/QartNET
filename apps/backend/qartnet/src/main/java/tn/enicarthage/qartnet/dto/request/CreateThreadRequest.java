package tn.enicarthage.qartnet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record CreateThreadRequest(
        @NotBlank @Size(min = 5, max = 200) String title,
        @NotBlank @Size(min = 10) String body,
        @NotNull UUID categoryPublicId,
        List<@NotBlank @Size(max = 50) String> tagNames
) {}