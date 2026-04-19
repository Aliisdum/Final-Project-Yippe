package az.developia.flight_booking_name.config;

import az.developia.flight_booking_name.entity.Flight;
import az.developia.flight_booking_name.entity.Plane;
import az.developia.flight_booking_name.entity.Seat;
import az.developia.flight_booking_name.entity.SeatClass;
import az.developia.flight_booking_name.entity.User;
import az.developia.flight_booking_name.entity.UserRole;
import az.developia.flight_booking_name.repository.FlightRepository;
import az.developia.flight_booking_name.repository.PlaneRepository;
import az.developia.flight_booking_name.repository.SeatRepository;
import az.developia.flight_booking_name.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@AllArgsConstructor
@SuppressWarnings("null")
public class DataLoader {

    private UserRepository userRepository;
    private PlaneRepository planeRepository;
    private SeatRepository seatRepository;
    private FlightRepository flightRepository;
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            // Check if admin user already exists
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = User.builder()
                        .username("admin")
                        .email("admin@flightbooking.com")
                        .fullName("Admin User")
                        .password(passwordEncoder.encode("admin123"))
                        .role(UserRole.ADMIN)
                        .build();
                userRepository.save(admin);
                System.out.println("✓ Default admin account created");
                System.out.println("  Username: admin");
                System.out.println("  Password: admin123");
            }

            // Create a sample airline manager if not exists
            if (userRepository.findByUsername("manager").isEmpty()) {
                User manager = User.builder()
                        .username("manager")
                        .email("manager@flightbooking.com")
                        .fullName("Airline Manager")
                        .password(passwordEncoder.encode("manager123"))
                        .role(UserRole.AIRLINE_MANAGER)
                        .build();
                userRepository.save(manager);
                System.out.println("✓ Default airline manager account created");
                System.out.println("  Username: manager");
                System.out.println("  Password: manager123");
            }

            // Create a sample customer if not exists
            if (userRepository.findByUsername("customer").isEmpty()) {
                User customer = User.builder()
                        .username("customer")
                        .email("customer@flightbooking.com")
                        .fullName("Test Customer")
                        .password(passwordEncoder.encode("customer123"))
                        .role(UserRole.CUSTOMER)
                        .build();
                userRepository.save(customer);
                System.out.println("✓ Default customer account created");
                System.out.println("  Username: customer");
                System.out.println("  Password: customer123");
            }

            // Create sample planes if not exists
            if (planeRepository.count() == 0) {
                // Boeing 737
                Plane plane1 = Plane.builder()
                        .name("Boeing 737-800")
                        .totalSeats(189)
                        .totalBusinessSeats(30)
                        .totalEconomySeats(159)
                        .build();
                planeRepository.save(plane1);
                createSeats(plane1, 30, 159);

                // Airbus A320
                Plane plane2 = Plane.builder()
                        .name("Airbus A320-200")
                        .totalSeats(180)
                        .totalBusinessSeats(25)
                        .totalEconomySeats(155)
                        .build();
                planeRepository.save(plane2);
                createSeats(plane2, 25, 155);

                // Boeing 777
                Plane plane3 = Plane.builder()
                        .name("Boeing 777-300ER")
                        .totalSeats(350)
                        .totalBusinessSeats(60)
                        .totalEconomySeats(290)
                        .build();
                planeRepository.save(plane3);
                createSeats(plane3, 60, 290);

                // Airbus A380
                Plane plane4 = Plane.builder()
                        .name("Airbus A380-800")
                        .totalSeats(555)
                        .totalBusinessSeats(80)
                        .totalEconomySeats(475)
                        .build();
                planeRepository.save(plane4);
                createSeats(plane4, 80, 475);

                System.out.println("✓ Sample planes created: Boeing 737, Airbus A320, Boeing 777, Airbus A380");

                // Create sample flights
                if (flightRepository.count() == 0) {
                    createSampleFlights();
                }
            }
        };
    }

    private void createSampleFlights() {
        try {
            User manager = userRepository.findByUsername("manager").orElseThrow();
            List<Plane> planes = planeRepository.findAll();
            if (planes.isEmpty()) return;

            // Sample flight 1: Baku to Istanbul
            Flight flight1 = Flight.builder()
                    .flightNumber("AZ001")
                    .origin("Baku")
                    .destination("Istanbul")
                    .departureTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0))
                    .arrivalTime(LocalDateTime.now().plusDays(1).withHour(12).withMinute(30))
                    .price(BigDecimal.valueOf(150.00))
                    .plane(planes.get(0))
                    .airlineManager(manager)
                    .active(true)
                    .build();
            flightRepository.save(flight1);

            // Sample flight 2: Istanbul to London
            Flight flight2 = Flight.builder()
                    .flightNumber("AZ002")
                    .origin("Istanbul")
                    .destination("London")
                    .departureTime(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0))
                    .arrivalTime(LocalDateTime.now().plusDays(2).withHour(17).withMinute(0))
                    .price(BigDecimal.valueOf(300.00))
                    .plane(planes.get(1))
                    .airlineManager(manager)
                    .active(true)
                    .build();
            flightRepository.save(flight2);

            // Sample flight 3: London to New York
            Flight flight3 = Flight.builder()
                    .flightNumber("AZ003")
                    .origin("London")
                    .destination("New York")
                    .departureTime(LocalDateTime.now().plusDays(3).withHour(18).withMinute(0))
                    .arrivalTime(LocalDateTime.now().plusDays(3).withHour(6).withMinute(0).plusDays(1))
                    .price(BigDecimal.valueOf(800.00))
                    .plane(planes.get(2))
                    .airlineManager(manager)
                    .active(true)
                    .build();
            flightRepository.save(flight3);

            System.out.println("✓ Sample flights created: Baku-Istanbul, Istanbul-London, London-New York");
        } catch (Exception e) {
            System.out.println("Error creating sample flights: " + e.getMessage());
        }
    }

    private void createSeats(Plane plane, int businessSeats, int economySeats) {
        // Create business seats
        for (int i = 1; i <= businessSeats; i++) {
            String seatNumber = i + "A";
            Seat seat = Seat.builder()
                    .seatNumber(seatNumber)
                    .seatClass(SeatClass.BUSINESS)
                    .plane(plane)
                    .build();
            seatRepository.save(seat);
        }

        // Create economy seats
        for (int i = 1; i <= economySeats; i++) {
            String seatNumber = i + "B";
            Seat seat = Seat.builder()
                    .seatNumber(seatNumber)
                    .seatClass(SeatClass.ECONOMY)
                    .plane(plane)
                    .build();
            seatRepository.save(seat);
        }
    }
}
