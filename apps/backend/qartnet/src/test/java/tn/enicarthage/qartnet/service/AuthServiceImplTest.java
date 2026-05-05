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
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.repository.PasswordResetTokenRepository;
import tn.enicarthage.qartnet.repository.ProfileRepository;
import tn.enicarthage.qartnet.repository.UserRepository;
import tn.enicarthage.qartnet.security.JwtService;
import tn.enicarthage.qartnet.service.impl.AuthServiceImpl;
import tn.enicarthage.qartnet.shared.exception.ConflictException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private EmailService emailService;

    @InjectMocks private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "frontendUrl", "http://localhost:4200");
    }

    @Test
    void register_validRequest_returnsTokenResponse() {
        RegisterRequest request = new RegisterRequest("Ela", "Fadhli", null, null, "ela@enicar.ucar.tn", "password123");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(profileRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        verify(userRepository).save(any(User.class));
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void register_duplicateEmail_throwsConflictException() {
        RegisterRequest request = new RegisterRequest("Ela", "Fadhli", null, null, "ela@enicar.ucar.tn", "password123");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email");
    }

    @Test
    void login_validCredentials_returnsTokenResponse() {
        LoginRequest request = new LoginRequest("ela@enicar.ucar.tn", "password123");

        User user = new User();
        user.setPublicId(UUID.randomUUID());
        user.setEmail(request.email());
        user.setPassword("hashed");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
    }

    @Test
    void login_userNotFound_throwsBadCredentialsException() {
        LoginRequest request = new LoginRequest("unknown@enicar.ucar.tn", "password123");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    // --- forgotPassword ---

    @Test
    void forgotPassword_userExists_savesTokenAndSendsEmail() {
        User user = new User();
        user.setPublicId(UUID.randomUUID());
        user.setEmail("ela@enicar.ucar.tn");
        ForgotPasswordRequest request = new ForgotPasswordRequest("ela@enicar.ucar.tn");

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
        ForgotPasswordRequest request = new ForgotPasswordRequest("ghost@enicar.ucar.tn");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        authService.forgotPassword(request);

        verifyNoInteractions(passwordResetTokenRepository);
        verifyNoInteractions(emailService);
    }

    // --- resetPassword ---

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
