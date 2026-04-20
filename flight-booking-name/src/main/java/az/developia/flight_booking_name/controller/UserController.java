package az.developia.flight_booking_name.controller;

import az.developia.flight_booking_name.request.RegisterRequest;
import az.developia.flight_booking_name.response.ApiResponse;
import az.developia.flight_booking_name.response.UserResponse;
import az.developia.flight_booking_name.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@AllArgsConstructor
@Tag(name = "Admin User Management", description = "Admin user management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private UserService userService;

    @PostMapping("/airline-managers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create airline manager", description = "Admin creates new AIRLINE_MANAGER")
    public ResponseEntity<ApiResponse<UserResponse>> createAirlineManager(@Valid @RequestBody RegisterRequest request) {
        // airlineId = current admin id or from request? Spec not specify, use null for now
        var user = userService.createAirlineManager(request, null);
        var response = userService.mapToUserResponse(user);  // need to add public mapToUserResponse to UserService

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<UserResponse>builder()
                        .success(true)
                        .message("Airline manager created successfully")
                        .data(response)
                        .statusCode(HttpStatus.CREATED.value())
                        .build());
    }

}
