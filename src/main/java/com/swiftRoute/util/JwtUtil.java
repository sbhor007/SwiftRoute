package com.swiftRoute.util;

import com.swiftRoute.entity.User;
import com.swiftRoute.service.RedisService;
import com.swiftRoute.service.TokenService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {
    @Value("${jwt.secret-key}")
    private String JWTSecretKey;
    @Getter
    @Value("${jwt.access-expiration-ms}")
    private long accessExpirationMs;
    @Getter
    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;
    private static final long CLOCK_SKEW_MS = 60_000; // 60 seconds tolerance for clock drift

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(JWTSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        return buildToken(user, accessExpirationMs);
    }

    public String generateRefreshToken(User user) {
        return buildToken(user, refreshExpirationMs);
    }
    private String buildToken(User user, long expirationMs) {
        String role = user.getRole() != null ? user.getRole().name() : "UNKNOWN";
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("UserId", user.getId().toString())
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSecretKey())
                .compact();
    }

    // extract email and roll from token
    public String getUsernameFromToken(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    private Claims getAllClaimsFromToken(String token) {
        // Parse JWT using the secret key and extract payload (Claims)
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .clock(() -> new Date(System.currentTimeMillis() - CLOCK_SKEW_MS)) // Handle clock skew
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validates JWT token integrity, signature, and expiration status
     * @param token Raw JWT string (without "Bearer " prefix)
     * @return true if token is valid AND not expired (considering clock skew)
     */
    public boolean validateJwtStructure(String token) {

        try {
            // Parse and validate signature + standard claims (exp, nbf)
            Claims claims = this.getAllClaimsFromToken(token);

            // CRITICAL: Validate subject claim exists and is non-empty
            String subject = claims.getSubject();
            if (subject == null || subject.trim().isEmpty()) {
                log.warn("JWT validation failed: Subject claim is missing or empty in token");
                return false;
            }
            return true;

        } catch (SignatureException e) {
            log.error("JWT validation failed: Invalid signature", e);

        } catch (MalformedJwtException e) {
            log.error("JWT validation failed: Malformed token structure", e);
        } catch (ExpiredJwtException e) {
            // Explicitly caught even with clock skew handling for audit trail
            log.warn("JWT validation failed: Token expired (exp={})",
                    e.getClaims() != null ? e.getClaims().getExpiration() : "unknown");
        } catch (UnsupportedJwtException e) {
            log.error("JWT validation failed: Unsupported JWT format/version", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT validation failed: Token argument invalid", e);
        } catch (SecurityException e) {
            log.error("JWT validation failed: Security violation (possible tampering)", e);
        } catch (Exception e) {
            log.error("JWT validation failed: Unexpected error", e);
        }
        return false;
    }

    // Extract Role from JWT token
    public String getRoleFromToken(String token) {
        return getAllClaimsFromToken(token).get("role", String.class);
    }

    public long getExpirationTime(String token){
        return getAllClaimsFromToken(token).getExpiration().getTime();
    }

}
