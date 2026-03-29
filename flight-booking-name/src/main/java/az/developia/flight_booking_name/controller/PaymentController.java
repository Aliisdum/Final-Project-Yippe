package az.developia.flight_booking_name.controller;

import az.developia.flight_booking_name.request.ProcessPaymentRequest;
import az.developia.flight_booking_name.response.ApiResponse;
import az.developia.flight_booking_name.response.PaymentResponse;
import az.developia.flight_booking_name.service.PaymentService;
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
@RequestMapping("/api/payments")
@AllArgsConstructor
@Tag(name = "Payment Management", description = "Payment Processing APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {

    private PaymentService paymentService;

    @PostMapping("/bookings/{bookingId}/pay")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Process payment for booking", description = "Process payment for a flight booking")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @PathVariable Long bookingId,
            @Valid @RequestBody ProcessPaymentRequest request) {
        var payment = paymentService.processPayment(bookingId, request);
        var response = paymentService.getPaymentResponse(payment.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<PaymentResponse>builder()
                        .success(true)
                        .message(request.getSuccess() ? "Payment processed successfully" : "Payment failed")
                        .data(response)
                        .statusCode(HttpStatus.CREATED.value())
                        .build());
    }

    @GetMapping("/bookings/{bookingId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Get payment details", description = "Get payment details for a booking")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByBookingId(@PathVariable Long bookingId) {
        var payment = paymentService.getPaymentByBookingId(bookingId);
        var response = paymentService.getPaymentResponse(payment.getId());

        return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
                .success(true)
                .message("Payment retrieved successfully")
                .data(response)
                .statusCode(HttpStatus.OK.value())
                .build());
    }
}
