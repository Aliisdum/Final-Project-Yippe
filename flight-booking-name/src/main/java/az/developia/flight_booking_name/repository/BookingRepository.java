package az.developia.flight_booking_name.repository;

import az.developia.flight_booking_name.entity.Booking;
import az.developia.flight_booking_name.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByCustomerId(Long customerId, Pageable pageable);
    List<Booking> findByCustomerId(Long customerId);
    Optional<Booking> findByFlightIdAndSeatId(Long flightId, Long seatId);
    List<Booking> findByFlightId(Long flightId);
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);
    int countByFlightIdAndStatus(Long flightId, BookingStatus status);
}
