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
import tn.enicarthage.qartnet.dto.request.LoginRequest;
import tn.enicarthage.qartnet.dto.request.RegisterRequest;
import tn.enicarthage.qartnet.dto.response.AuthResponse;
import tn.enicarthage.qartnet.model.RefreshToken;
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.repository.*;
import tn.enicarthage.qartnet.security.JwtService;
import tn.enicarthage.qartnet.service.impl.AuthServiceImpl;
import tn.enicarthage.qartnet.shared.enums.AccountStatus;
import tn.enicarthage.qartnet.shared.enums.Role;
import tn.enicarthage.qartnet.shared.exception.BadRequestException;
import tn.enicarthage.qartnet.shared.exception.ConflictException;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

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

    @Test
    void register_validRequest_savesUserAndSendsEmail() {
        RegisterRequest request = new RegisterRequest(
                "student1", "student1@enicarthage.rnu.tn", "password123", "Alice", "Smith");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(activityLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.register(request);

        verify(userRepository).save(argThat(u ->
                u.getEmail().equals(request.email()) &&
                u.getAccountStatus() == AccountStatus.PENDING &&
                !u.isEmailVerified()
        ));
        verify(emailService).sendEmailVerification(eq(request.email()), anyString());
    }

    @Test
    void register_invalidDomain_throwsBadRequestException() {
        RegisterRequest request = new RegisterRequest("ela", "ela@gmail.com", "password123", null, null);
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void register_duplicateEmail_throwsConflictException() {
        RegisterRequest request = new RegisterRequest(
                "ela", "ela@enicarthage.rnu.tn", "password123", null, null);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email");
    }

    @Test
    void register_duplicateUsername_throwsConflictException() {
        RegisterRequest request = new RegisterRequest(
                "ela", "ela@enicarthage.rnu.tn", "password123", null, null);
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
}
