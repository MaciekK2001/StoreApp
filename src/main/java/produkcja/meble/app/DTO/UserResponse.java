package produkcja.meble.app.DTO;

import produkcja.meble.app.Entities.AccountRole;
import produkcja.meble.app.Entities.UserAccount;

public record UserResponse(
        Long id,
        String username,
        String email,
        AccountRole role
) {
    public static UserResponse from(UserAccount userAccount) {
        return new UserResponse(
                userAccount.getId(),
                userAccount.getUsername(),
                userAccount.getEmail(),
                userAccount.getRole()
        );
    }
}
