package tn.enicarthage.qartnet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import tn.enicarthage.qartnet.service.IAuthService;
import tn.enicarthage.qartnet.service.IEmailService;
import tn.enicarthage.qartnet.shared.exception.ConflictException;
import tn.enicarthage.qartnet.shared.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final IEmailService emailService;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email is already in use");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setDateOfBirth(request.dateOfBirth());
        user.setPhoneNumber(request.phoneNumber());
        userRepository.save(user);

        Profile profile = new Profile();
        profile.setUsername(generateUniqueUsername(request.email()));
        profile.setUser(user);
        profileRepository.save(profile);

        return new AuthResponse(jwtService.generateToken(user));
    }

    private String generateUniqueUsername(String email) {
        String base = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9_]", "");
        if (base.isEmpty()) base = "user";
        String candidate = base;
        int attempt = 0;
        while (profileRepository.existsByUsername(candidate)) {
            if (++attempt > 10) {
                candidate = base + UUID.randomUUID().toString().substring(0, 6);
                break;
            }
            candidate = base + (int) (Math.random() * 9000 + 1000);
        }
        return candidate;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getPublicId().toString(),
                        request.password()
                )
        );

        return new AuthResponse(jwtService.generateToken(user));
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            passwordResetTokenRepository.deleteAllByUser(user);

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(UUID.randomUUID().toString());
            resetToken.setUser(user);
            resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
            passwordResetTokenRepository.save(resetToken);

            String resetLink = frontendUrl + "/auth/reset-password?token=" + resetToken.getToken();
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(request.token())
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired reset token"));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Invalid or expired reset token");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}
