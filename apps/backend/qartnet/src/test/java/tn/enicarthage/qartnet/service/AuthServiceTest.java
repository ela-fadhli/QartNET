package tn.enicarthage.qartnet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import tn.enicarthage.qartnet.dto.request.*;
import tn.enicarthage.qartnet.dto.response.AuthResponse;
import tn.enicarthage.qartnet.model.PasswordResetToken;
import tn.enicarthage.qartnet.model.RefreshToken;
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.repository.*;
import tn.enicarthage.qartnet.security.JwtService;
import tn.enicarthage.qartnet.service.impl.AuthServiceImpl;
import tn.enicarthage.qartnet.shared.enums.AccountStatus;
import tn.enicarthage.qartnet.shared.enums.Role;
import tn.enicarthage.qartnet.shared.exception.BadRequestException;
import tn.enicarthage.qartnet.shared.exception.ConflictException;
import tn.enicarthage.qartnet.shared.exception.InvalidTokenException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;
    @Mock PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock ActivityLogRepository activityLogRepository;
    @Mock IEmailService emailService;

    @InjectMocks AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "frontendUrl", "http://localhost:4200");
        ReflectionTestUtils.setField(authService, "emailDomain", "enicarthage.rnu.tn");
        ReflectionTestUtils.setField(authService, "verificationExpirationHours", 24);
        ReflectionTestUtils.setField(authService, "refreshTokenExpirationMs", 604800000L);
    }

    // --- register ---

    @Test
    void register_shouldFail_whenEmailDomainIsInvalid() {
        RegisterRequest req = new RegisterRequest("user1", "user1@gmail.com", "Password1!", null, null);
        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("institutional email");
    }

    @Test
    void register_shouldFail_whenEmailAlreadyExists() {
        RegisterRequest req = new RegisterRequest("user1", "user1@enicarthage.rnu.tn", "Password1!", null, null);
        when(userRepository.existsByEmail(req.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email");
    }

    @Test
    void register_shouldFail_whenUsernameAlreadyExists() {
        RegisterRequest req = new RegisterRequest("user1", "user1@enicarthage.rnu.tn", "Password1!", null, null);
        when(userRepository.existsByEmail(req.email())).thenReturn(false);
        when(userRepository.existsByUsername(req.username())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Username");
    }

    @Test
    void register_shouldSaveUser_andSendVerificationEmail() {
        RegisterRequest req = new RegisterRequest("user1", "user1@enicarthage.rnu.tn", "Password1!", "Alice", "Smith");

        when(userRepository.existsByEmail(req.email())).thenReturn(false);
        when(userRepository.existsByUsername(req.username())).thenReturn(false);
        when(passwordEncoder.encode(req.password())).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(activityLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.register(req);

        verify(userRepository).save(argThat(u ->
                u.getEmail().equals(req.email()) &&
                u.getAccountStatus() == AccountStatus.PENDING &&
                !u.isEmailVerified()
        ));
        verify(emailService).sendEmailVerification(eq(req.email()), anyString());
    }

    // --- verifyEmail ---

    @Test
    void verifyEmail_shouldActivateAccount_whenTokenValid() {
        User user = buildUser();
        String token = "valid-token";
        user.setEmailVerificationToken(token);
        user.setEmailVerificationExpiry(LocalDateTime.now().plusHours(1));

        when(userRepository.findByEmailVerificationToken(token)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(activityLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.verifyEmail(token);

        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(user.getEmailVerificationToken()).isNull();
    }

    @Test
    void verifyEmail_shouldFail_whenTokenExpired() {
        User user = buildUser();
        String token = "expired-token";
        user.setEmailVerificationToken(token);
        user.setEmailVerificationExpiry(LocalDateTime.now().minusHours(1));

        when(userRepository.findByEmailVerificationToken(token)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.verifyEmail(token))
                .isInstanceOf(InvalidTokenException.class);
    }

    // --- refreshToken ---

    @Test
    void refreshToken_shouldReturnNewTokens_whenRefreshTokenValid() {
        User user = buildUser();
        RefreshToken stored = RefreshToken.builder()
                .token("valid-refresh")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();

        when(refreshTokenRepository.findByToken("valid-refresh")).thenReturn(Optional.of(stored));
        when(jwtService.generateToken(user)).thenReturn("new-access-token");
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> {
            RefreshToken rt = inv.getArgument(0);
            rt.setToken("new-refresh-token");
            return rt;
        });

        RefreshTokenRequest req = new RefreshTokenRequest("valid-refresh");
        AuthResponse response = authService.refreshToken(req);

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isNotNull();
    }

    @Test
    void refreshToken_shouldFail_whenTokenExpired() {
        RefreshToken expired = RefreshToken.builder()
                .token("expired")
                .user(buildUser())
                .expiryDate(Instant.now().minusSeconds(1))
                .build();

        when(refreshTokenRepository.findByToken("expired")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> authService.refreshToken(new RefreshTokenRequest("expired")))
                .isInstanceOf(InvalidTokenException.class);
    }

    // --- forgotPassword ---

    @Test
    void forgotPassword_shouldNotRevealUserExistence_whenEmailNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThatCode(() -> authService.forgotPassword(new ForgotPasswordRequest("unknown@enicarthage.rnu.tn")))
                .doesNotThrowAnyException();
        verifyNoInteractions(emailService);
    }

    // --- helpers ---

    private User buildUser() {
        User u = new User();
        u.setId(1L);
        u.setPublicId(UUID.randomUUID());
        u.setUsername("testuser");
        u.setEmail("test@enicarthage.rnu.tn");
        u.setPassword("hashed");
        u.setRoles(new HashSet<>(Set.of(Role.STUDENT)));
        u.setAccountStatus(AccountStatus.ACTIVE);
        u.setEmailVerified(true);
        return u;
    }
}
