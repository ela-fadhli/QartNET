package tn.enicarthage.qartnet.service;

import tn.enicarthage.qartnet.dto.request.UpdateProfileRequest;
import tn.enicarthage.qartnet.dto.response.ProfileResponse;

import java.util.UUID;

public interface ProfileService {

    ProfileResponse getMyProfile(UUID publicId);

    ProfileResponse updateMyProfile(UUID publicId, UpdateProfileRequest request);

    ProfileResponse getPublicProfile(String username);
}
