package vn.hcmute.webpr.jwt.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import vn.hcmute.webpr.jwt.config.JwtProperties;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {
    private final JwtService jwtService = new JwtService(properties());

    @Test
    void generatesAndVerifiesHs256Token() {
        User user = new User("student@hcmute.edu.vn", "password", List.of());
        String token = jwtService.generateToken(user);

        assertEquals("student@hcmute.edu.vn", jwtService.extractUsername(token));
        assertEquals(true, jwtService.isTokenValid(token, user));
    }

    @Test
    void rejectsTamperedToken() {
        User user = new User("student@hcmute.edu.vn", "password", List.of());
        String token = jwtService.generateToken(user);
        int signatureStart = token.lastIndexOf('.') + 1;
        char original = token.charAt(signatureStart);
        char replacement = original == 'a' ? 'b' : 'a';
        String tampered = token.substring(0, signatureStart) + replacement + token.substring(signatureStart + 1);

        assertThrows(InvalidJwtException.class, () -> jwtService.extractUsername(tampered));
    }

    @Test
    void rejectsExpiredToken() {
        JwtProperties expiredProperties = properties();
        expiredProperties.setExpirationTime(-1);
        JwtService expiredService = new JwtService(expiredProperties);
        User user = new User("student@hcmute.edu.vn", "password", List.of());

        assertThrows(InvalidJwtException.class, () -> expiredService.extractUsername(expiredService.generateToken(user)));
    }

    private static JwtProperties properties() {
        JwtProperties properties = new JwtProperties();
        properties.setSecretKey(Base64.getEncoder().encodeToString(
                "test-only-secret-key-with-32-bytes".getBytes(StandardCharsets.UTF_8)));
        properties.setExpirationTime(3_600_000);
        properties.setIssuer("jwt-nimbus-demo");
        properties.setAudience("jwt-nimbus-client");
        return properties;
    }
}
