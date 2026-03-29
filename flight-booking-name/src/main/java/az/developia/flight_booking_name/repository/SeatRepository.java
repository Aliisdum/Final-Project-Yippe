package az.developia.flight_booking_name.repository;

import az.developia.flight_booking_name.entity.Seat;
import az.developia.flight_booking_name.entity.SeatClass;
import az.developia.flight_booking_name.entity.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    Optional<Seat> findByPlaneIdAndSeatNumber(Long planeId, String seatNumber);
    List<Seat> findByPlaneId(Long planeId);
    List<Seat> findByPlaneIdAndStatus(Long planeId, SeatStatus status);
    List<Seat> findByPlaneIdAndSeatClassAndStatus(Long planeId, SeatClass seatClass, SeatStatus status);
    int countByPlaneIdAndStatus(Long planeId, SeatStatus status);
}
