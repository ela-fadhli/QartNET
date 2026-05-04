package tn.enicarthage.qartnet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.qartnet.dto.request.UpdateProfileRequest;
import tn.enicarthage.qartnet.dto.response.ProfileResponse;
import tn.enicarthage.qartnet.model.Profile;
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.repository.ProfileRepository;
import tn.enicarthage.qartnet.repository.UserRepository;
import tn.enicarthage.qartnet.service.ProfileService;
import tn.enicarthage.qartnet.shared.exception.ConflictException;
import tn.enicarthage.qartnet.shared.exception.ResourceNotFoundException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    @Override
    @Transactional
    public ProfileResponse getMyProfile(UUID publicId) {
        User user = findUser(publicId);
        Profile profile = findProfile(user);
        return toResponse(user, profile);
    }

    @Override
    @Transactional
    public ProfileResponse updateMyProfile(UUID publicId, UpdateProfileRequest request) {
        User user = findUser(publicId);
        Profile profile = findProfile(user);

        if (request.username() != null && !request.username().equals(profile.getUsername())) {
            if (profileRepository.existsByUsername(request.username())) {
                throw new ConflictException("Username is already taken");
            }
            profile.setUsername(request.username());
        }
        if (request.bio() != null)               profile.setBio(request.bio());
        if (request.profilePictureUrl() != null)  profile.setProfilePictureUrl(request.profilePictureUrl());
        if (request.firstName() != null)          user.setFirstName(request.firstName());
        if (request.lastName() != null)           user.setLastName(request.lastName());
        if (request.dateOfBirth() != null)        user.setDateOfBirth(request.dateOfBirth());
        if (request.phoneNumber() != null)        user.setPhoneNumber(request.phoneNumber());

        profileRepository.save(profile);
        userRepository.save(user);

        return toResponse(user, profile);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getPublicProfile(String username) {
        Profile profile = profileRepository.findByUsername(username)
                .orElseThrow(() -> ResourceNotFoundException.of("Profile", username));
        return toResponse(profile.getUser(), profile);
    }

    private User findUser(UUID publicId) {
        return userRepository.findByPublicId(publicId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", publicId));
    }

    private Profile findProfile(User user) {
        return profileRepository.findByUser(user).orElseGet(() -> {
            Profile profile = new Profile();
            profile.setUser(user);
            profile.setUsername(user.getUsername());
            return profileRepository.save(profile);
        });
    }

    private ProfileResponse toResponse(User user, Profile profile) {
        return new ProfileResponse(
                profile.getUsername(),
                profile.getBio(),
                profile.getProfilePictureUrl(),
                user.getFirstName(),
                user.getLastName(),
                user.getDateOfBirth(),
                user.getPhoneNumber()
        );
    }
}
