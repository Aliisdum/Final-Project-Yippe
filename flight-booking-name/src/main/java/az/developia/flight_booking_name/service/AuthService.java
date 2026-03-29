package az.developia.flight_booking_name.service;

import az.developia.flight_booking_name.entity.User;
import az.developia.flight_booking_name.exception.UnauthorizedException;
import az.developia.flight_booking_name.repository.UserRepository;
import az.developia.flight_booking_name.request.LoginRequest;
import az.developia.flight_booking_name.request.RegisterRequest;
import az.developia.flight_booking_name.response.LoginResponse;
import az.developia.flight_booking_name.util.JwtTokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
public class AuthService {

    private UserService userService;
    private UserRepository userRepository;
    private JwtTokenProvider jwtTokenProvider;
    private PasswordEncoder passwordEncoder;

    public LoginResponse register(RegisterRequest request) {
        User user = userService.registerUser(request);
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().toString());

        return LoginResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().toString())
                .token(token)
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        if (!user.getEnabled()) {
            throw new UnauthorizedException("User account is disabled");
        }

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().toString());

        return LoginResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().toString())
                .token(token)
                .build();
    }
}
