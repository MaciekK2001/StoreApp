package produkcja.meble.app.Services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import produkcja.meble.app.DTO.AuthResponse;
import produkcja.meble.app.DTO.LoginRequest;
import produkcja.meble.app.DTO.RegisterRequest;
import produkcja.meble.app.DTO.UserResponse;
import produkcja.meble.app.Entities.AccountRole;
import produkcja.meble.app.Entities.UserAccount;
import produkcja.meble.app.Exceptions.BadRequestException;
import produkcja.meble.app.Repositories.UserAccountRepository;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userAccountRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username is already taken");
        }

        if (userAccountRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email is already taken");
        }

        UserAccount userAccount = UserAccount.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(AccountRole.USER)
                .build();

        UserAccount savedUser = userAccountRepository.save(userAccount);
        String token = jwtService.generateToken(savedUser);

        return new AuthResponse("Bearer", token, UserResponse.from(savedUser));
    }

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
