package vn.hcmute.webpr.jwt.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import vn.hcmute.webpr.jwt.config.JwtProperties;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {
    private final JwtProperties properties;
    private final byte[] secret;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        try {
            this.secret = Base64.getDecoder().decode(properties.getSecretKey());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("security.jwt.secret-key must be Base64 encoded", exception);
        }
        if (secret.length < 32) {
            throw new IllegalStateException("security.jwt.secret-key must decode to at least 32 bytes for HS256");
        }
    }

    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(properties.getExpirationTime());
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(userDetails.getUsername())
                .issuer(properties.getIssuer())
                .audience(properties.getAudience())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(expiresAt))
                .jwtID(UUID.randomUUID().toString())
                .build();
        try {
            SignedJWT jwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(), claims);
            jwt.sign(new MACSigner(secret));
            return jwt.serialize();
        } catch (JOSEException exception) {
            throw new IllegalStateException("Unable to sign JWT", exception);
        }
    }

    public String extractUsername(String token) {
        return verifiedClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername());
    }

    public long getExpirationTime() {
        return properties.getExpirationTime();
    }

    private JWTClaimsSet verifiedClaims(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm())) {
                throw new InvalidJwtException("JWT algorithm is not allowed");
            }
            if (!JOSEObjectType.JWT.equals(jwt.getHeader().getType())) {
                throw new InvalidJwtException("JWT type is not allowed");
            }
            if (!jwt.verify(new MACVerifier(secret))) {
                throw new InvalidJwtException("JWT signature is invalid");
            }
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            if (claims.getSubject() == null || claims.getSubject().isBlank()) {
                throw new InvalidJwtException("JWT subject is missing");
            }
            if (!properties.getIssuer().equals(claims.getIssuer())) {
                throw new InvalidJwtException("JWT issuer is invalid");
            }
            if (!List.of(properties.getAudience()).equals(claims.getAudience())) {
                throw new InvalidJwtException("JWT audience is invalid");
            }
            Date expiration = claims.getExpirationTime();
            if (expiration == null || !expiration.toInstant().isAfter(Instant.now())) {
                throw new InvalidJwtException("JWT has expired");
            }
            return claims;
        } catch (ParseException | JOSEException exception) {
            throw new InvalidJwtException("JWT is malformed", exception);
        }
    }
}
