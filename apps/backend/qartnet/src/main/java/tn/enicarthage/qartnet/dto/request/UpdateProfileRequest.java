package tn.enicarthage.qartnet.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileRequest(
        @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
        String username,

        @Size(max = 300, message = "Bio must not exceed 300 characters")
        String bio,

        String profilePictureUrl,

        @Size(max = 50, message = "First name must not exceed 50 characters")
        String firstName,

        @Size(max = 50, message = "Last name must not exceed 50 characters")
        String lastName,

        LocalDate dateOfBirth,

        @Pattern(regexp = "^\\+?[0-9 \\-]{7,20}$", message = "Invalid phone number format")
        String phoneNumber
) {}
