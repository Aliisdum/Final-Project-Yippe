package az.developia.flight_booking_name.service;

import az.developia.flight_booking_name.entity.Booking;
import az.developia.flight_booking_name.entity.BookingStatus;
import az.developia.flight_booking_name.entity.Payment;
import az.developia.flight_booking_name.entity.PaymentStatus;
import az.developia.flight_booking_name.exception.BadRequestException;
import az.developia.flight_booking_name.exception.ResourceNotFoundException;
import az.developia.flight_booking_name.repository.BookingRepository;
import az.developia.flight_booking_name.repository.PaymentRepository;
import az.developia.flight_booking_name.request.ProcessPaymentRequest;
import az.developia.flight_booking_name.response.PaymentResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class PaymentService {

    private PaymentRepository paymentRepository;
    private BookingRepository bookingRepository;

    public Payment processPayment(Long bookingId, ProcessPaymentRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getPayment() != null) {
            throw new BadRequestException("Payment has already been processed for this booking");
        }

        String transactionId = UUID.randomUUID().toString();
        PaymentStatus status = request.getSuccess() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;

        Payment payment = Payment.builder()
                .booking(booking)
                .amount(booking.getTotalPrice())
                .status(status)
                .transactionId(transactionId)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // If payment is successful, update booking status to CONFIRMED
        if (request.getSuccess()) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
        }

        return savedPayment;
    }

    public Payment getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for booking"));
    }

    public PaymentResponse getPaymentResponse(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        return mapToPaymentResponse(payment);
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBooking() != null ? payment.getBooking().getId() : null)
                .amount(payment.getAmount())
                .status(payment.getStatus().toString())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
