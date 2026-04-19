package az.developia.flight_booking_name.service;

import az.developia.flight_booking_name.entity.*;
import az.developia.flight_booking_name.exception.BadRequestException;
import az.developia.flight_booking_name.exception.ConflictException;
import az.developia.flight_booking_name.exception.ResourceNotFoundException;
import az.developia.flight_booking_name.repository.BookingRepository;
import az.developia.flight_booking_name.repository.SeatRepository;
import az.developia.flight_booking_name.request.CreateBookingRequest;
import az.developia.flight_booking_name.response.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class BookingService {

    private BookingRepository bookingRepository;
    private FlightService flightService;
    private UserService userService;
    private SeatRepository seatRepository;

    public Booking createBooking(CreateBookingRequest request, Long customerId) {
        // Get entities
        Flight flight = flightService.getFlightById(request.getFlightId());
        User customer = userService.getUserById(customerId);
        Seat seat = seatRepository.findById(request.getSeatId())
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found"));

        // Validate that seat belongs to the flight's plane
        if (!seat.getPlane().getId().equals(flight.getPlane().getId())) {
            throw new BadRequestException("Seat does not belong to this flight");
        }

        // Check if seat is available
        if (seat.getStatus() == SeatStatus.BOOKED) {
            throw new ConflictException("Seat is already booked");
        }

        // Check if same seat is already booked for this flight
        bookingRepository.findByFlightIdAndSeatId(request.getFlightId(), request.getSeatId())
                .ifPresent(b -> {
                    throw new ConflictException("This seat is already booked for this flight");
                });

        // Create booking
        Booking booking = Booking.builder()
                .customer(customer)
                .flight(flight)
                .seat(seat)
                .passengerName(request.getPassengerName())
                .totalPrice(flight.getPrice())
                .status(BookingStatus.PENDING)
                .build();

        // Update seat status
        seat.setStatus(SeatStatus.BOOKED);
        seatRepository.save(seat);

        return bookingRepository.save(booking);
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    public Page<BookingResponse> getMyBookings(Long customerId, Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findByCustomerId(customerId, pageable);
        List<BookingResponse> responses = bookings.getContent().stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, bookings.getTotalElements());
    }

    public void cancelBooking(Long bookingId, Long customerId) {
        Booking booking = getBookingById(bookingId);

        // Check authorization
        if (!booking.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("You are not authorized to cancel this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }

        // Update booking status
        booking.setStatus(BookingStatus.CANCELLED);

        // Free up the seat
        Seat seat = booking.getSeat();
        seat.setStatus(SeatStatus.AVAILABLE);
        seatRepository.save(seat);

        bookingRepository.save(booking);
    }

    public Page<BookingResponse> getAllBookings(Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findAll(pageable);
        List<BookingResponse> responses = bookings.getContent().stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, bookings.getTotalElements());
    }

    public BookingResponse getBookingResponseById(Long id) {
        Booking booking = getBookingById(id);
        return mapToBookingResponse(booking);
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        UserResponse customerResponse = UserResponse.builder()
                .id(booking.getCustomer().getId())
                .username(booking.getCustomer().getUsername())
                .email(booking.getCustomer().getEmail())
                .fullName(booking.getCustomer().getFullName())
                .role(booking.getCustomer().getRole().toString())
                .build();

        SeatResponse seatResponse = SeatResponse.builder()
                .id(booking.getSeat().getId())
                .seatNumber(booking.getSeat().getSeatNumber())
                .seatClass(booking.getSeat().getSeatClass().toString())
                .status(booking.getSeat().getStatus().toString())
                .createdAt(booking.getSeat().getCreatedAt())
                .updatedAt(booking.getSeat().getUpdatedAt())
                .build();

        UserResponse managerResponse = UserResponse.builder()
                .id(booking.getFlight().getAirlineManager().getId())
                .username(booking.getFlight().getAirlineManager().getUsername())
                .email(booking.getFlight().getAirlineManager().getEmail())
                .fullName(booking.getFlight().getAirlineManager().getFullName())
                .role(booking.getFlight().getAirlineManager().getRole().toString())
                .build();

        List<SeatResponse> seatResponses = booking.getFlight().getPlane().getSeats().stream()
                .map(seat -> SeatResponse.builder()
                        .id(seat.getId())
                        .seatNumber(seat.getSeatNumber())
                        .seatClass(seat.getSeatClass().toString())
                        .status(seat.getStatus().toString())
                        .createdAt(seat.getCreatedAt())
                        .updatedAt(seat.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        PlaneResponse planeResponse = PlaneResponse.builder()
                .id(booking.getFlight().getPlane().getId())
                .name(booking.getFlight().getPlane().getName())
                .totalSeats(booking.getFlight().getPlane().getTotalSeats())
                .totalBusinessSeats(booking.getFlight().getPlane().getTotalBusinessSeats())
                .totalEconomySeats(booking.getFlight().getPlane().getTotalEconomySeats())
                .seats(seatResponses)
                .createdAt(booking.getFlight().getPlane().getCreatedAt())
                .updatedAt(booking.getFlight().getPlane().getUpdatedAt())
                .build();

        FlightResponse flightResponse = FlightResponse.builder()
                .id(booking.getFlight().getId())
                .flightNumber(booking.getFlight().getFlightNumber())
                .origin(booking.getFlight().getOrigin())
                .destination(booking.getFlight().getDestination())
                .departureTime(booking.getFlight().getDepartureTime())
                .arrivalTime(booking.getFlight().getArrivalTime())
                .price(booking.getFlight().getPrice())
                .plane(planeResponse)
                .airlineManager(managerResponse)
                .active(booking.getFlight().getActive())
                .createdAt(booking.getFlight().getCreatedAt())
                .updatedAt(booking.getFlight().getUpdatedAt())
                .build();

        PaymentResponse paymentResponse = null;
        if (booking.getPayment() != null) {
            paymentResponse = PaymentResponse.builder()
                    .id(booking.getPayment().getId())
                    .amount(booking.getPayment().getAmount())
                    .status(booking.getPayment().getStatus().toString())
                    .transactionId(booking.getPayment().getTransactionId())
                    .createdAt(booking.getPayment().getCreatedAt())
                    .updatedAt(booking.getPayment().getUpdatedAt())
                    .build();
        }

        return BookingResponse.builder()
                .id(booking.getId())
                .customer(customerResponse)
                .flight(flightResponse)
                .seat(seatResponse)
                .passengerName(booking.getPassengerName())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus().toString())
                .payment(paymentResponse)
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
