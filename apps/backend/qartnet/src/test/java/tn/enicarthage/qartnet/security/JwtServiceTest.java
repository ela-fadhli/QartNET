package tn.enicarthage.qartnet.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import tn.enicarthage.qartnet.model.User;
import tn.enicarthage.qartnet.shared.enums.Role;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    // 64-byte base64-encoded key for HS512
    private static final String TEST_SECRET =
            "dGVzdHNlY3JldGtleWZvcnVuaXR0ZXN0c3RoYXRpc2xvbmdlbm91Z2hmb3JoczUxMg==";
    private static final long TEST_EXPIRATION = 3_600_000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", TEST_EXPIRATION);
    }

    private User buildUser() {
        User user = new User();
        user.setPublicId(UUID.randomUUID());
        user.setEmail("ela@enicar.ucar.tn");
        user.setRole(Role.USER);
        return user;
    }

    @Test
    void generateToken_returnsNonEmptyToken() {
        String token = jwtService.generateToken(buildUser());

        assertThat(token).isNotBlank();
    }

    @Test
    void extractPublicId_returnsCorrectSubject() {
        User user = buildUser();
        String token = jwtService.generateToken(user);

        String extracted = jwtService.extractPublicId(token);

        assertThat(extracted).isEqualTo(user.getPublicId().toString());
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtService.generateToken(buildUser());

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_tamperedToken_returnsFalse() {
        String token = jwtService.generateToken(buildUser());
        String tampered = token.substring(0, token.length() - 4) + "XXXX";

        assertThat(jwtService.isTokenValid(tampered)).isFalse();
    }
}
