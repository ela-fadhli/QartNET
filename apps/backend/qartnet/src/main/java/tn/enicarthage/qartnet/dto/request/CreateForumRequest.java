package tn.enicarthage.qartnet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateForumRequest(
        @NotBlank @Size(min = 3, max = 100) String name,
        @NotBlank @Pattern(regexp = "^[a-z0-9-]{3,100}$", message = "Slug must be lowercase alphanumeric with hyphens") String slug,
        @Size(max = 500) String description,
        String banner
) {}
