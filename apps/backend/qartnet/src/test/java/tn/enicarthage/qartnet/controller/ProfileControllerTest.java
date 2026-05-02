package tn.enicarthage.qartnet.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tn.enicarthage.qartnet.dto.request.UpdateProfileRequest;
import tn.enicarthage.qartnet.dto.response.ProfileResponse;
import tn.enicarthage.qartnet.security.JwtService;
import tn.enicarthage.qartnet.service.IProfileService;
import tn.enicarthage.qartnet.shared.exception.ConflictException;
import tn.enicarthage.qartnet.shared.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

    @Autowired private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean private IProfileService profileService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserDetailsService userDetailsService;

    private static final String USER_UUID = "550e8400-e29b-41d4-a716-446655440000";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    private ProfileResponse sampleProfile() {
        return new ProfileResponse(
                "ela.fadhli", "Hello world", null,
                "Ela", "Fadhli", LocalDate.of(2000, 1, 1), "+216 12 345 678"
        );
    }

    @Test
    @WithMockUser(username = USER_UUID)
    void getMyProfile_authenticated_returns200WithProfileData() throws Exception {
        when(profileService.getMyProfile(UUID.fromString(USER_UUID))).thenReturn(sampleProfile());

        mockMvc.perform(get("/api/profile/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("ela.fadhli"))
                .andExpect(jsonPath("$.data.firstName").value("Ela"))
                .andExpect(jsonPath("$.data.lastName").value("Fadhli"));
    }

    @Test
    void getMyProfile_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/profile/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = USER_UUID)
    void updateMyProfile_validRequest_returns200WithUpdatedData() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "new.username", "New bio", null, "New", "Name", null, null
        );
        ProfileResponse updated = new ProfileResponse(
                "new.username", "New bio", null, "New", "Name", null, null
        );

        when(profileService.updateMyProfile(eq(UUID.fromString(USER_UUID)), any())).thenReturn(updated);

        mockMvc.perform(put("/api/profile/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("new.username"))
                .andExpect(jsonPath("$.data.bio").value("New bio"));
    }

    @Test
    @WithMockUser(username = USER_UUID)
    void updateMyProfile_usernameTooShort_returns400() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "ab", null, null, null, null, null, null
        );

        mockMvc.perform(put("/api/profile/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = USER_UUID)
    void updateMyProfile_bioTooLong_returns400() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest(
                null, "x".repeat(301), null, null, null, null, null
        );

        mockMvc.perform(put("/api/profile/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = USER_UUID)
    void updateMyProfile_usernameTaken_returns409() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "taken.username", null, null, null, null, null, null
        );

        when(profileService.updateMyProfile(any(), any()))
                .thenThrow(new ConflictException("Username is already taken"));

        mockMvc.perform(put("/api/profile/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username is already taken"));
    }

    @Test
    @WithMockUser(username = USER_UUID)
    void getMyProfile_profileNotFound_returns404() throws Exception {
        when(profileService.getMyProfile(UUID.fromString(USER_UUID)))
                .thenThrow(new ResourceNotFoundException("Profile not found"));

        mockMvc.perform(get("/api/profile/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
