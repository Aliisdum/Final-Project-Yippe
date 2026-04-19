package az.developia.flight_booking_name.repository;

import az.developia.flight_booking_name.entity.Flight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    Optional<Flight> findByFlightNumber(String flightNumber);
    List<Flight> findByAirlineManagerId(Long airlineManagerId);
    Page<Flight> findByAirlineManagerId(Long airlineManagerId, Pageable pageable);
    
    @Query("SELECT f FROM Flight f WHERE f.active = true")
    Page<Flight> findAllActive(Pageable pageable);
    
    @Query("SELECT f FROM Flight f WHERE f.origin = :origin AND f.destination = :destination AND f.active = true")
    Page<Flight> searchByOriginAndDestination(@Param("origin") String origin, 
                                               @Param("destination") String destination, 
                                               Pageable pageable);
    
    @Query("SELECT f FROM Flight f WHERE f.origin = :origin AND f.destination = :destination " +
           "AND DATE(f.departureTime) = :date AND f.active = true")
    Page<Flight> searchByOriginDestinationAndDate(@Param("origin") String origin, 
                                                   @Param("destination") String destination,
                                                   @Param("date") LocalDateTime date,
                                                   Pageable pageable);
    
    @Query("SELECT f FROM Flight f WHERE f.origin = :origin AND f.destination = :destination " +
           "AND f.price BETWEEN :minPrice AND :maxPrice AND f.active = true")
    Page<Flight> searchByOriginDestinationAndPrice(@Param("origin") String origin, 
                                                    @Param("destination") String destination,
                                                    @Param("minPrice") java.math.BigDecimal minPrice,
                                                    @Param("maxPrice") java.math.BigDecimal maxPrice,
                                                    Pageable pageable);
}
