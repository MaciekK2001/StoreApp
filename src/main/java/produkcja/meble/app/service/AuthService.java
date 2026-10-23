package produkcja.meble.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import produkcja.meble.app.dto.AuthResponse;
import produkcja.meble.app.dto.LoginRequest;
import produkcja.meble.app.dto.UserResponse;
import produkcja.meble.app.entity.UserAccount;
import produkcja.meble.app.exception.BadRequestException;
import produkcja.meble.app.repository.UserAccountRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserAccount userAccount = userAccountRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));
        String token = jwtService.generateToken(userAccount);

        return new AuthResponse("Bearer", token, UserResponse.from(userAccount));
    }
}
