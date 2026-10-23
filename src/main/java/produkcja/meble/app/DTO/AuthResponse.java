package produkcja.meble.app.dto;

public record AuthResponse(
        String tokenType,
        String token,
        UserResponse user
) {
}
