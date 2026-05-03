package tn.enicarthage.qartnet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.qartnet.dto.request.UpdateProfileRequest;
import tn.enicarthage.qartnet.dto.response.UserProfileResponse;
import tn.enicarthage.qartnet.dto.response.UserPublicProfileResponse;
import tn.enicarthage.qartnet.model.ActivityLog;
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.repository.ActivityLogRepository;
import tn.enicarthage.qartnet.repository.UserRepository;
import tn.enicarthage.qartnet.service.IUserService;
import tn.enicarthage.qartnet.shared.enums.ActivityType;
import tn.enicarthage.qartnet.shared.exception.ResourceNotFoundException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;

    @Override
    public UserProfileResponse getMyProfile(String publicIdStr) {
        User user = findByPublicId(UUID.fromString(publicIdStr));
        return toProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(String publicIdStr, UpdateProfileRequest request) {
        User user = findByPublicId(UUID.fromString(publicIdStr));

        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.bio() != null) user.setBio(request.bio());
        if (request.profilePictureUrl() != null) user.setProfilePictureUrl(request.profilePictureUrl());
        if (request.skills() != null) {
            user.getSkills().clear();
            user.getSkills().addAll(request.skills());
        }
        if (request.socialLinks() != null) {
            user.getSocialLinks().clear();
            user.getSocialLinks().putAll(request.socialLinks());
        }

        userRepository.save(user);

        activityLogRepository.save(ActivityLog.builder()
                .user(user)
                .action(ActivityType.PROFILE_UPDATED)
                .build());

        return toProfileResponse(user);
    }

    @Override
    public UserPublicProfileResponse getPublicProfile(UUID publicId) {
        User user = findByPublicId(publicId);
        return new UserPublicProfileResponse(
                user.getPublicId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getBio(),
                user.getProfilePictureUrl(),
                user.getSkills(),
                user.getCreatedAt()
        );
    }

    private User findByPublicId(UUID publicId) {
        return userRepository.findByPublicId(publicId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> ResourceNotFoundException.of("User", publicId));
    }

    private UserProfileResponse toProfileResponse(User u) {
        return new UserProfileResponse(
                u.getPublicId(),
                u.getUsername(),
                u.getEmail(),
                u.getFirstName(),
                u.getLastName(),
                u.getBio(),
                u.getProfilePictureUrl(),
                u.getSkills(),
                u.getSocialLinks(),
                u.getRoles(),
                u.getAccountStatus(),
                u.isEmailVerified(),
                u.getCreatedAt(),
                u.getLastLoginAt()
        );
    }
}
