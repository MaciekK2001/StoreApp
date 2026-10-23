package produkcja.meble.app.dto;

import produkcja.meble.app.entity.AccountRole;
import produkcja.meble.app.entity.UserAccount;

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
