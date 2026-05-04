package tn.enicarthage.qartnet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import tn.enicarthage.qartnet.dto.request.ForgotPasswordRequest;
import tn.enicarthage.qartnet.dto.request.LoginRequest;
import tn.enicarthage.qartnet.dto.request.RegisterRequest;
import tn.enicarthage.qartnet.dto.request.ResetPasswordRequest;
import tn.enicarthage.qartnet.dto.response.AuthResponse;
import tn.enicarthage.qartnet.model.PasswordResetToken;
import tn.enicarthage.qartnet.model.Profile;
import tn.enicarthage.qartnet.model.RefreshToken;
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.repository.ActivityLogRepository;
import tn.enicarthage.qartnet.repository.PasswordResetTokenRepository;
import tn.enicarthage.qartnet.repository.ProfileRepository;
import tn.enicarthage.qartnet.repository.RefreshTokenRepository;
import tn.enicarthage.qartnet.repository.UserRepository;
import tn.enicarthage.qartnet.security.JwtService;
import tn.enicarthage.qartnet.service.impl.AuthServiceImpl;
import tn.enicarthage.qartnet.shared.enums.AccountStatus;
import tn.enicarthage.qartnet.shared.enums.Role;
import tn.enicarthage.qartnet.shared.exception.BadRequestException;
import tn.enicarthage.qartnet.shared.exception.ConflictException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private ActivityLogRepository activityLogRepository;
    @Mock private EmailService emailService;

    @InjectMocks AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "frontendUrl", "http://localhost:4200");
        ReflectionTestUtils.setField(authService, "emailDomain", "enicarthage.rnu.tn");
        ReflectionTestUtils.setField(authService, "verificationExpirationHours", 24);
        ReflectionTestUtils.setField(authService, "refreshTokenExpirationMs", 604800000L);
    }

    @Test
    void register_validRequest_savesUserAndSendsEmail() {
        RegisterRequest request = new RegisterRequest(
                "student1", "Alice", "Smith", null, null,
                "student1@enicarthage.rnu.tn", "password123");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(activityLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.register(request);

        verify(userRepository).save(argThat(u ->
                u.getEmail().equals(request.email()) &&
                u.getAccountStatus() == AccountStatus.PENDING &&
                !u.isEmailVerified()
        ));
        verify(profileRepository).save(any(Profile.class));
        verify(emailService).sendEmailVerification(eq(request.email()), anyString());
    }

    @Test
    void register_invalidDomain_throwsBadRequestException() {
        RegisterRequest request = new RegisterRequest(
                "ela", null, null, null, null, "ela@gmail.com", "password123");
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void register_duplicateEmail_throwsConflictException() {
        RegisterRequest request = new RegisterRequest(
                "ela", null, null, null, null, "ela@enicarthage.rnu.tn", "password123");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email");
    }

    @Test
    void register_duplicateUsername_throwsConflictException() {
        RegisterRequest request = new RegisterRequest(
                "ela", null, null, null, null, "ela@enicarthage.rnu.tn", "password123");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Username");
    }

    @Test
    void login_validCredentials_returnsTokenResponse() {
        LoginRequest request = new LoginRequest("student1@enicarthage.rnu.tn", "password123");

        User user = new User();
        user.setPublicId(UUID.randomUUID());
        user.setEmail(request.email());
        user.setPassword("hashed");
        user.setRoles(new HashSet<>(Set.of(Role.STUDENT)));
        user.setAccountStatus(AccountStatus.ACTIVE);

        RefreshToken refreshToken = RefreshToken.builder()
                .token("refresh-xyz")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(604800))
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(userRepository.save(any())).thenReturn(user);
        when(refreshTokenRepository.save(any())).thenReturn(refreshToken);
        when(activityLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-xyz");
    }

    @Test
    void login_userNotFound_throwsBadCredentialsException() {
        LoginRequest request = new LoginRequest("unknown@enicarthage.rnu.tn", "password123");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void forgotPassword_userExists_savesTokenAndSendsEmail() {
        User user = new User();
        user.setPublicId(UUID.randomUUID());
        user.setEmail("ela@enicarthage.rnu.tn");
        ForgotPasswordRequest request = new ForgotPasswordRequest("ela@enicarthage.rnu.tn");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        authService.forgotPassword(request);

        verify(passwordResetTokenRepository).deleteAllByUser(user);
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq(user.getEmail()), anyString());
    }

    @Test
    void forgotPassword_userNotFound_doesNothing() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("ghost@enicarthage.rnu.tn");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        authService.forgotPassword(request);

        verifyNoInteractions(passwordResetTokenRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    void resetPassword_validToken_changesPasswordAndMarksUsed() {
        User user = new User();
        user.setPublicId(UUID.randomUUID());
        user.setPassword("old-hash");

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken("valid-token");
        resetToken.setUser(user);
        resetToken.setUsed(false);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));

        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "newPassword123");

        when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("newPassword123")).thenReturn("new-hash");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(passwordResetTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.resetPassword(request);

        assertThat(user.getPassword()).isEqualTo("new-hash");
        assertThat(resetToken.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).save(resetToken);
    }

    @Test
    void resetPassword_tokenNotFound_throwsBadCredentialsException() {
        when(passwordResetTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("missing", "pass")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void resetPassword_expiredToken_throwsBadCredentialsException() {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken("expired-token");
        resetToken.setUser(new User());
        resetToken.setUsed(false);
        resetToken.setExpiresAt(LocalDateTime.now().minusHours(1));

        when(passwordResetTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(resetToken));

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("expired-token", "pass")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void resetPassword_usedToken_throwsBadCredentialsException() {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken("used-token");
        resetToken.setUser(new User());
        resetToken.setUsed(true);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));

        when(passwordResetTokenRepository.findByToken("used-token")).thenReturn(Optional.of(resetToken));

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("used-token", "pass")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
