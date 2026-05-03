package tn.enicarthage.qartnet.service;

import tn.enicarthage.qartnet.dto.request.UpdateProfileRequest;
import tn.enicarthage.qartnet.dto.response.UserProfileResponse;
import tn.enicarthage.qartnet.dto.response.UserPublicProfileResponse;

import java.util.UUID;

public interface IUserService {

    UserProfileResponse getMyProfile(String publicId);

    UserProfileResponse updateMyProfile(String publicId, UpdateProfileRequest request);

    UserPublicProfileResponse getPublicProfile(UUID publicId);
}
