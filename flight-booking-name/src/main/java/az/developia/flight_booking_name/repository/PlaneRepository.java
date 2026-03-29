package az.developia.flight_booking_name.repository;

import az.developia.flight_booking_name.entity.Plane;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlaneRepository extends JpaRepository<Plane, Long> {
    Optional<Plane> findByName(String name);
    boolean existsByName(String name);
}
