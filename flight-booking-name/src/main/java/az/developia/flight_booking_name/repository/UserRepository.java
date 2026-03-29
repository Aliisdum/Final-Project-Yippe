package az.developia.flight_booking_name.repository;

import az.developia.flight_booking_name.entity.User;
import az.developia.flight_booking_name.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
    List<User> findByAirlineIdAndRole(Long airlineId, UserRole role);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
