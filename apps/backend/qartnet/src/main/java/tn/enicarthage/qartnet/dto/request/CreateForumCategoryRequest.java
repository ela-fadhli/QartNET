package tn.enicarthage.qartnet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateForumCategoryRequest(
        @NotBlank @Size(min = 2, max = 80) String name
) {}
