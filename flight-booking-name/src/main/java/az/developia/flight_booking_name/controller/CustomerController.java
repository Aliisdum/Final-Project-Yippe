package az.developia.flight_booking_name.controller;

import az.developia.flight_booking_name.entity.User;
import az.developia.flight_booking_name.exception.UnauthorizedException;
import az.developia.flight_booking_name.response.ApiResponse;
import az.developia.flight_booking_name.response.BookingResponse;
import az.developia.flight_booking_name.response.UserResponse;
import az.developia.flight_booking_name.service.BookingService;
import az.developia.flight_booking_name.service.FileStorageService;
import az.developia.flight_booking_name.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = {"http://localhost:8080", "http://127.0.0.1:8080"})
@AllArgsConstructor
@Tag(name = "Customer Management", description = "Customer profile APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerController {

    private UserService userService;
    private BookingService bookingService;
    private FileStorageService fileStorageService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        return userService.getUserByUsername(authentication.getName()).getId();
    }

    private UserResponse mapToUserResponse(az.developia.flight_booking_name.entity.User user) {
        String fullName = user.getFullName() != null ? user.getFullName().trim() : "";
        String[] nameParts = fullName.split(" ");
        String firstName = nameParts.length > 0 ? nameParts[0] : "";
        String lastName = nameParts.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(nameParts, 1, nameParts.length)) : "";

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(fullName)
                .name(firstName)
                .surname(lastName)
                .profilePictureUrl(user.getProfilePictureUrl())
                .role(user.getRole().toString())
                .build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get current customer profile", description = "Returns current authenticated customer profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile() {
        Long userId = getCurrentUserId();
        var user = userService.getUserById(userId);
        var response = mapToUserResponse(user);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Customer profile retrieved successfully")
                .data(response)
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @GetMapping("/me/bookings")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get current customer bookings", description = "Returns bookings for the current authenticated customer")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long customerId = getCurrentUserId();
        var bookings = bookingService.getMyBookings(customerId, org.springframework.data.domain.PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.<Page<BookingResponse>>builder()
                .success(true)
                .message("Customer bookings retrieved successfully")
                .data(bookings)
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @PostMapping(value = "/upload-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Upload profile picture", description = "Upload or update the customer's profile picture")
    public ResponseEntity<ApiResponse<UserResponse>> uploadProfilePicture(@RequestPart("file") MultipartFile file) {
        Long userId = getCurrentUserId();
        var user = userService.getUserById(userId);
        String oldPicture = user.getProfilePictureUrl();
        String fileName = fileStorageService.storeFile(file);
        var updatedUser = userService.updateProfilePicture(userId, fileName);
        if (oldPicture != null && !oldPicture.isBlank()) {
            fileStorageService.deleteFile(oldPicture);
        }
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Profile picture uploaded successfully")
                .data(mapToUserResponse(updatedUser))
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @DeleteMapping("/delete-picture")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Delete profile picture", description = "Delete the customer's profile picture")
    public ResponseEntity<ApiResponse<UserResponse>> deleteProfilePicture() {
        Long userId = getCurrentUserId();
        var user = userService.getUserById(userId);
        String pictureUrl = user.getProfilePictureUrl();
        if (pictureUrl != null && !pictureUrl.isBlank()) {
            fileStorageService.deleteFile(pictureUrl);
            userService.updateProfilePicture(userId, null);
        }
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Profile picture deleted successfully")
                .data(mapToUserResponse(userService.getUserById(userId)))
                .statusCode(HttpStatus.OK.value())
                .build());
    }
}
