package tn.enicarthage.qartnet.dto.request;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.List;
import java.util.Map;

public record UpdateProfileRequest(

        @Size(max = 50, message = "First name must be at most 50 characters")
        String firstName,

        @Size(max = 50, message = "Last name must be at most 50 characters")
        String lastName,

        @Size(max = 1000, message = "Bio must be at most 1000 characters")
        String bio,

        @URL(message = "Profile picture must be a valid URL")
        String profilePictureUrl,

        List<@Size(max = 50) String> skills,

        Map<String, String> socialLinks
) {}
