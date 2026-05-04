package tn.enicarthage.qartnet.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record UpdateProfileRequest(

        @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
        String username,

        @Size(max = 50, message = "First name must be at most 50 characters")
        String firstName,

        @Size(max = 50, message = "Last name must be at most 50 characters")
        String lastName,

        @Size(max = 1000, message = "Bio must be at most 1000 characters")
        String bio,

        @URL(message = "Profile picture must be a valid URL")
        String profilePictureUrl,

        List<@Size(max = 50) String> skills,

        Map<String, String> socialLinks,

        LocalDate dateOfBirth,

        @Pattern(regexp = "^\\+?[0-9 \\-]{7,20}$", message = "Invalid phone number format")
        String phoneNumber
) {}
