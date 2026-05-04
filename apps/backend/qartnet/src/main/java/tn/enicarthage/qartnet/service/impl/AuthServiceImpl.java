package tn.enicarthage.qartnet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enicarthage.qartnet.dto.request.*;
import tn.enicarthage.qartnet.dto.response.AuthResponse;
import tn.enicarthage.qartnet.model.ActivityLog;
import tn.enicarthage.qartnet.model.PasswordResetToken;
import tn.enicarthage.qartnet.model.Profile;
import tn.enicarthage.qartnet.model.RefreshToken;
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.repository.*;
import tn.enicarthage.qartnet.security.JwtService;
import tn.enicarthage.qartnet.service.AuthService;
import tn.enicarthage.qartnet.service.EmailService;
import tn.enicarthage.qartnet.shared.enums.AccountStatus;
import tn.enicarthage.qartnet.shared.enums.ActivityType;
import tn.enicarthage.qartnet.shared.enums.Role;
import tn.enicarthage.qartnet.shared.exception.BadRequestException;
import tn.enicarthage.qartnet.shared.exception.ConflictException;
import tn.enicarthage.qartnet.shared.exception.InvalidTokenException;
import tn.enicarthage.qartnet.shared.exception.ResourceNotFoundException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ActivityLogRepository activityLogRepository;
    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${app.email.domain-whitelist:enicarthage.rnu.tn}")
    private String emailDomain;

    @Value("${app.email.verification-expiration-hours:24}")
    private int verificationExpirationHours;

    @Value("${app.refresh-token.expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        validateInstitutionalEmail(request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email is already in use");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username is already taken");
        }

        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .publicId(UUID.randomUUID())
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dateOfBirth(request.dateOfBirth())
                .phoneNumber(request.phoneNumber())
                .roles(Set.of(Role.STUDENT))
                .accountStatus(AccountStatus.PENDING)
                .emailVerified(false)
                .emailVerificationToken(verificationToken)
                .emailVerificationExpiry(LocalDateTime.now().plusHours(verificationExpirationHours))
                .build();

        userRepository.save(user);

        Profile profile = new Profile();
        profile.setUsername(request.username());
        profile.setUser(user);
        profileRepository.save(profile);

        String verificationLink = frontendUrl + "/auth/verify-email?token=" + verificationToken;
        emailService.sendEmailVerification(user.getEmail(), verificationLink);

        logActivity(user, ActivityType.USER_REGISTER, null, null);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getPublicId().toString(),
                        request.password()
                )
        );

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = createOrReplaceRefreshToken(user);

        logActivity(user, ActivityType.USER_LOGIN, null, null);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(InvalidTokenException::refreshToken);

        if (stored.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(stored);
            throw InvalidTokenException.refreshToken();
        }

        User user = stored.getUser();
        String newAccessToken = jwtService.generateToken(user);
        RefreshToken newRefresh = createOrReplaceRefreshToken(user);

        return new AuthResponse(newAccessToken, newRefresh.getToken());
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByToken(request.refreshToken())
                .ifPresent(rt -> {
                    logActivity(rt.getUser(), ActivityType.USER_LOGOUT, null, null);
                    refreshTokenRepository.delete(rt);
                });
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(InvalidTokenException::verificationToken);

        if (user.getEmailVerificationExpiry().isBefore(LocalDateTime.now())) {
            throw InvalidTokenException.verificationToken();
        }

        user.setEmailVerified(true);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiry(null);
        userRepository.save(user);

        logActivity(user, ActivityType.EMAIL_VERIFIED, null, null);
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

            logActivity(user, ActivityType.PASSWORD_RESET_REQUEST, null, null);
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(request.token())
                .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Invalid or expired reset token"));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new org.springframework.security.authentication.BadCredentialsException("Invalid or expired reset token");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        refreshTokenRepository.deleteByUser(user);

        logActivity(user, ActivityType.PASSWORD_RESET_COMPLETE, null, null);
    }

    private void validateInstitutionalEmail(String email) {
        if (!email.toLowerCase().endsWith("@" + emailDomain.toLowerCase())) {
            throw new BadRequestException(
                    "Only institutional email addresses (@" + emailDomain + ") are accepted");
        }
    }

    private RefreshToken createOrReplaceRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .build();
        return refreshTokenRepository.save(token);
    }

    private void logActivity(User user, ActivityType type, String details, String ip) {
        ActivityLog log = ActivityLog.builder()
                .user(user)
                .action(type)
                .details(details)
                .ipAddress(ip)
                .build();
        activityLogRepository.save(log);
    }
}
