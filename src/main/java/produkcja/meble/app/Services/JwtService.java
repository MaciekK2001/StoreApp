package produkcja.meble.app.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import produkcja.meble.app.Entities.UserAccount;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private final String secret;
    private final long expirationMillis;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-millis}") long expirationMillis
    ) {
        this.secret = secret;
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(UserAccount userAccount) {
        Instant now = Instant.now();

        Map<String, String> header = Map.of(
                "alg", "HS256",
                "typ", "JWT"
        );

        Map<String, String> payload = Map.of(
                "sub", userAccount.getUsername(),
                "userId", userAccount.getId().toString(),
                "role", userAccount.getRole().name(),
                "iat", String.valueOf(now.getEpochSecond()),
                "exp", String.valueOf(now.plusMillis(expirationMillis).getEpochSecond())
        );

        String encodedHeader = encodeJson(header);
        String encodedPayload = encodeJson(payload);
        String unsignedToken = encodedHeader + "." + encodedPayload;

        return unsignedToken + "." + sign(unsignedToken);
    }

    public String extractUsername(String token) {
        String subject = extractClaims(token).get("sub");
        return subject == null ? null : subject;
    }

    public boolean isTokenValid(String token, UserAccount userAccount) {
        String username = extractUsername(token);
        return userAccount.getUsername().equals(username) && !isExpired(token) && hasValidSignature(token);
    }

    private boolean isExpired(String token) {
        String expiration = extractClaims(token).get("exp");
        long expirationSeconds = Long.parseLong(expiration);
        return Instant.now().isAfter(Instant.ofEpochSecond(expirationSeconds));
    }

    private boolean hasValidSignature(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        String unsignedToken = parts[0] + "." + parts[1];
        return sign(unsignedToken).equals(parts[2]);
    }

    private Map<String, String> extractClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid token");
            }

            byte[] payload = BASE64_URL_DECODER.decode(parts[1]);
            return parseFlatJson(new String(payload, StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid token", exception);
        }
    }

    private String encodeJson(Map<String, String> value) {
        String json = value.entrySet().stream()
                .map(entry -> "\"" + escapeJson(entry.getKey()) + "\":\"" + escapeJson(entry.getValue()) + "\"")
                .collect(Collectors.joining(",", "{", "}"));

        return BASE64_URL_ENCODER.encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private Map<String, String> parseFlatJson(String json) {
        Pattern pattern = Pattern.compile("\"((?:\\\\.|[^\"])*)\":\"((?:\\\\.|[^\"])*)\"");
        Matcher matcher = pattern.matcher(json);
        Map<String, String> values = new HashMap<>();

        while (matcher.find()) {
            values.put(unescapeJson(matcher.group(1)), unescapeJson(matcher.group(2)));
        }

        return values;
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String unescapeJson(String value) {
        return value.replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(keySpec);
            return BASE64_URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not sign token", exception);
        }
    }
}
