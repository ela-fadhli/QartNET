package tn.enicarthage.qartnet.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.enicarthage.qartnet.dto.request.UpdateProfileRequest;
import tn.enicarthage.qartnet.dto.response.ProfileResponse;
import tn.enicarthage.qartnet.model.Profile;
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.repository.ProfileRepository;
import tn.enicarthage.qartnet.repository.UserRepository;
import tn.enicarthage.qartnet.service.impl.ProfileServiceImpl;
import tn.enicarthage.qartnet.shared.exception.ConflictException;
import tn.enicarthage.qartnet.shared.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private ProfileRepository profileRepository;

    @InjectMocks private ProfileServiceImpl profileService;

    private static final UUID PUBLIC_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private User buildUser() {
        User user = new User();
        user.setPublicId(PUBLIC_ID);
        user.setEmail("ela@test.com");
        user.setFirstName("Ela");
        user.setLastName("Fadhli");
        user.setDateOfBirth(LocalDate.of(2000, 1, 1));
        user.setPhoneNumber("+216 12 345 678");
        return user;
    }

    private Profile buildProfile(User user) {
        Profile profile = new Profile();
        profile.setUsername("ela.fadhli");
        profile.setBio("Hello world");
        profile.setUser(user);
        return profile;
    }

    // --- getMyProfile ---

    @Test
    void getMyProfile_userAndProfileExist_returnsMappedResponse() {
        User user = buildUser();
        Profile profile = buildProfile(user);

        when(userRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.of(user));
        when(profileRepository.findByUser(user)).thenReturn(Optional.of(profile));

        ProfileResponse response = profileService.getMyProfile(PUBLIC_ID);

        assertThat(response.username()).isEqualTo("ela.fadhli");
        assertThat(response.bio()).isEqualTo("Hello world");
        assertThat(response.firstName()).isEqualTo("Ela");
        assertThat(response.lastName()).isEqualTo("Fadhli");
        assertThat(response.dateOfBirth()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(response.phoneNumber()).isEqualTo("+216 12 345 678");
    }

    @Test
    void getMyProfile_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getMyProfile(PUBLIC_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    void getMyProfile_profileNotFound_throwsResourceNotFoundException() {
        User user = buildUser();
        when(userRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.of(user));
        when(profileRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getMyProfile(PUBLIC_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Profile");
    }

    // --- updateMyProfile ---

    @Test
    void updateMyProfile_allFieldsProvided_updatesUserAndProfile() {
        User user = buildUser();
        Profile profile = buildProfile(user);
        UpdateProfileRequest request = new UpdateProfileRequest(
                "ela.fadhli", "Updated bio", null,
                "Updated", "Name", LocalDate.of(1999, 6, 15), "+216 99 999 999"
        );

        when(userRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.of(user));
        when(profileRepository.findByUser(user)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileResponse response = profileService.updateMyProfile(PUBLIC_ID, request);

        assertThat(response.bio()).isEqualTo("Updated bio");
        assertThat(response.firstName()).isEqualTo("Updated");
        assertThat(response.lastName()).isEqualTo("Name");
        assertThat(response.dateOfBirth()).isEqualTo(LocalDate.of(1999, 6, 15));
        assertThat(response.phoneNumber()).isEqualTo("+216 99 999 999");
        verify(profileRepository).save(profile);
        verify(userRepository).save(user);
    }

    @Test
    void updateMyProfile_newUsernameAvailable_changesUsername() {
        User user = buildUser();
        Profile profile = buildProfile(user);
        UpdateProfileRequest request = new UpdateProfileRequest(
                "new.username", null, null, null, null, null, null
        );

        when(userRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.of(user));
        when(profileRepository.findByUser(user)).thenReturn(Optional.of(profile));
        when(profileRepository.existsByUsername("new.username")).thenReturn(false);
        when(profileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProfileResponse response = profileService.updateMyProfile(PUBLIC_ID, request);

        assertThat(response.username()).isEqualTo("new.username");
        assertThat(profile.getUsername()).isEqualTo("new.username");
    }

    @Test
    void updateMyProfile_sameUsername_skipsConflictCheck() {
        User user = buildUser();
        Profile profile = buildProfile(user);
        UpdateProfileRequest request = new UpdateProfileRequest(
                "ela.fadhli", null, null, null, null, null, null
        );

        when(userRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.of(user));
        when(profileRepository.findByUser(user)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        profileService.updateMyProfile(PUBLIC_ID, request);

        verify(profileRepository, never()).existsByUsername(any());
    }

    @Test
    void updateMyProfile_usernameTaken_throwsConflictException() {
        User user = buildUser();
        Profile profile = buildProfile(user);
        UpdateProfileRequest request = new UpdateProfileRequest(
                "taken.username", null, null, null, null, null, null
        );

        when(userRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.of(user));
        when(profileRepository.findByUser(user)).thenReturn(Optional.of(profile));
        when(profileRepository.existsByUsername("taken.username")).thenReturn(true);

        assertThatThrownBy(() -> profileService.updateMyProfile(PUBLIC_ID, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Username");

        verify(profileRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateMyProfile_nullFields_doesNotOverwriteExistingValues() {
        User user = buildUser();
        Profile profile = buildProfile(user);
        UpdateProfileRequest request = new UpdateProfileRequest(
                null, null, null, null, null, null, null
        );

        when(userRepository.findByPublicId(PUBLIC_ID)).thenReturn(Optional.of(user));
        when(profileRepository.findByUser(user)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProfileResponse response = profileService.updateMyProfile(PUBLIC_ID, request);

        assertThat(response.username()).isEqualTo("ela.fadhli");
        assertThat(response.bio()).isEqualTo("Hello world");
        assertThat(response.firstName()).isEqualTo("Ela");
        verify(profileRepository, never()).existsByUsername(any());
    }
}
