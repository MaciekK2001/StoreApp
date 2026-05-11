package produkcja.meble.app.DTO;

public record AuthResponse(
        String tokenType,
        String token,
        UserResponse user
) {
}
