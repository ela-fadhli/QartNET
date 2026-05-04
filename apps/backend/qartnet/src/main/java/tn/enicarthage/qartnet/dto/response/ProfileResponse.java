package tn.enicarthage.qartnet.dto.response;

import java.time.LocalDate;

public record ProfileResponse(
        String username,
        String bio,
        String profilePictureUrl,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String phoneNumber
) {}
