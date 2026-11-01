package produkcja.meble.app.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import produkcja.meble.app.entity.UserAccount;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMillis;

    public JwtService(
            @Value("${app.jwt.secret:}") String secret,
            @Value("${app.jwt.expiration-millis:86400000}") long expirationMillis
    ) {
        if (secret == null || secret.trim().isBlank()) {
            throw new IllegalStateException(
                    "Zmienna środowiskowa JWT_SECRET nie została ustawiona! " +
                    "Wymagane jest podanie bezpiecznego klucza w zmiennych środowiskowych o długości minimum 32 znaków (256 bitów)."
            );
        }

        byte[] secretBytes = secret.trim().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "Klucz JWT_SECRET jest za krótki (" + secretBytes.length + " bajtów). " +
                    "Algorytm HMAC-SHA256 wymaga klucza o długości minimum 32 bajtów (256 bitów)."
            );
        }

        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(UserAccount userAccount) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userAccount.getUsername())
                .claim("userId", userAccount.getId())
                .claim("role", userAccount.getRole().name())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMillis))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserAccount userAccount) {
        try {
            String username = extractUsername(token);
            return userAccount.getUsername().equals(username) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
