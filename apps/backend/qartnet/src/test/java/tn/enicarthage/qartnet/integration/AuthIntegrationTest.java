package tn.enicarthage.qartnet.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.qartnet.dto.request.LoginRequest;
import tn.enicarthage.qartnet.dto.request.RegisterRequest;
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.repository.UserRepository;
import tn.enicarthage.qartnet.service.IEmailService;
import tn.enicarthage.qartnet.shared.enums.AccountStatus;
import tn.enicarthage.qartnet.shared.enums.Role;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @MockBean IEmailService emailService;

    @BeforeEach
    void setUp() {
        doNothing().when(emailService).sendEmailVerification(anyString(), anyString());
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void register_shouldReturn201_whenRequestValid() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "newuser", "newuser@enicarthage.rnu.tn", "Password1!", "Alice", "Smith");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void register_shouldReturn400_whenEmailDomainInvalid() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "baduser", "baduser@gmail.com", "Password1!", null, null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void register_shouldReturn409_whenEmailAlreadyExists() throws Exception {
        createActiveUser("existinguser", "existing@enicarthage.rnu.tn");

        RegisterRequest req = new RegisterRequest(
                "newuser2", "existing@enicarthage.rnu.tn", "Password1!", null, null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_shouldReturn200_withTokens_whenCredentialsValid() throws Exception {
        createActiveUser("loginuser", "loginuser@enicarthage.rnu.tn");

        LoginRequest req = new LoginRequest("loginuser@enicarthage.rnu.tn", "Password1!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    void login_shouldReturn401_whenAccountNotVerified() throws Exception {
        User user = User.builder()
                .publicId(UUID.randomUUID())
                .username("pendinguser")
                .email("pending@enicarthage.rnu.tn")
                .password("$2a$10$mfakehashedpasswordxxx.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
                .roles(new java.util.HashSet<>(Set.of(Role.STUDENT)))
                .accountStatus(AccountStatus.PENDING)
                .emailVerified(false)
                .build();
        userRepository.save(user);

        LoginRequest req = new LoginRequest("pending@enicarthage.rnu.tn", "Password1!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_shouldReturn400_whenPasswordTooShort() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "user3", "user3@enicarthage.rnu.tn", "short", null, null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    private void createActiveUser(String username, String email) {
        User user = User.builder()
                .publicId(UUID.randomUUID())
                .username(username)
                .email(email)
                .password(passwordEncoder.encode("Password1!"))
                .roles(new java.util.HashSet<>(Set.of(Role.STUDENT)))
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(true)
                .build();
        userRepository.save(user);
    }
}
