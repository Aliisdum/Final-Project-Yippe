package az.developia.flight_booking_name.controller;

import az.developia.flight_booking_name.request.LoginRequest;
import az.developia.flight_booking_name.request.RegisterRequest;
import az.developia.flight_booking_name.response.ApiResponse;
import az.developia.flight_booking_name.response.LoginResponse;
import az.developia.flight_booking_name.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Tag(name = "Authentication", description = "User Authentication APIs")
public class AuthController {

    private AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Register a new customer user")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);

        return ResponseEntity.ok(ApiResponse.<LoginResponse>builder()
                .success(true)
                .message("User registered successfully")
                .data(response)
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.<LoginResponse>builder()
                .success(true)
                .message("Login successful")
                .data(response)
                .statusCode(HttpStatus.OK.value())
                .build());
    }
}
