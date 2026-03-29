package az.developia.flight_booking_name.controller;

import az.developia.flight_booking_name.request.CreateBookingRequest;
import az.developia.flight_booking_name.response.ApiResponse;
import az.developia.flight_booking_name.response.ApiResponse.PaginationInfo;
import az.developia.flight_booking_name.response.BookingResponse;
import az.developia.flight_booking_name.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@AllArgsConstructor
@Tag(name = "Booking Management", description = "Flight Booking APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class BookingController {

    private BookingService bookingService;

    private Long getCustomerId() {
        // This should be replaced with actual user ID from token
        return 1L;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create a new booking", description = "Create a flight booking")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody CreateBookingRequest request) {
        Long customerId = getCustomerId();
        var booking = bookingService.createBooking(request, customerId);
        var bookingPage = bookingService.getMyBookings(customerId, PageRequest.of(0, 1));
        var response = bookingPage.getContent().stream()
                .filter(b -> b.getId().equals(booking.getId()))
                .findFirst()
                .orElse(null);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<BookingResponse>builder()
                        .success(true)
                        .message("Booking created successfully")
                        .data(response)
                        .statusCode(HttpStatus.CREATED.value())
                        .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Get booking details", description = "Get detailed information about a booking")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable Long id) {
        var booking = bookingService.getBookingById(id);
        var bookingPage = bookingService.getAllBookings(PageRequest.of(0, 1));
        var response = bookingPage.getContent().stream()
                .filter(b -> b.getId().equals(booking.getId()))
                .findFirst()
                .orElse(null);

        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder()
                .success(true)
                .message("Booking retrieved successfully")
                .data(response)
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get my bookings", description = "Get all bookings for the current user")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long customerId = getCustomerId();
        Pageable pageable = PageRequest.of(page, size);
        Page<BookingResponse> bookings = bookingService.getMyBookings(customerId, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<BookingResponse>>builder()
                .success(true)
                .message("Bookings retrieved successfully")
                .data(bookings)
                .statusCode(HttpStatus.OK.value())
                .pagination(PaginationInfo.from(bookings))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Cancel a booking", description = "Cancel an existing booking")
    public ResponseEntity<ApiResponse<?>> cancelBooking(@PathVariable Long id) {
        Long customerId = getCustomerId();
        bookingService.cancelBooking(id, customerId);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Booking cancelled successfully")
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all bookings", description = "Get all bookings (Admin only)")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookingResponse> bookings = bookingService.getAllBookings(pageable);

        return ResponseEntity.ok(ApiResponse.<Page<BookingResponse>>builder()
                .success(true)
                .message("Bookings retrieved successfully")
                .data(bookings)
                .statusCode(HttpStatus.OK.value())
                .pagination(PaginationInfo.from(bookings))
                .build());
    }
}
