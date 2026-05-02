package tn.enicarthage.qartnet.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.enicarthage.qartnet.dto.request.LoginRequest;
import tn.enicarthage.qartnet.dto.request.RegisterRequest;
import tn.enicarthage.qartnet.dto.response.AuthResponse;
import tn.enicarthage.qartnet.model.Profile;
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.repository.PasswordResetTokenRepository;
import tn.enicarthage.qartnet.repository.ProfileRepository;
import tn.enicarthage.qartnet.repository.UserRepository;
import tn.enicarthage.qartnet.security.JwtService;
import tn.enicarthage.qartnet.service.impl.AuthServiceImpl;
import tn.enicarthage.qartnet.shared.exception.ConflictException;

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
    @Mock private tn.enicarthage.qartnet.service.IEmailService emailService;

    @InjectMocks private AuthServiceImpl authService;

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
}
