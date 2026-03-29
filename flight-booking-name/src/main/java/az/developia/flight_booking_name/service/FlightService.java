package az.developia.flight_booking_name.service;

import az.developia.flight_booking_name.entity.*;
import az.developia.flight_booking_name.exception.BadRequestException;
import az.developia.flight_booking_name.exception.ConflictException;
import az.developia.flight_booking_name.exception.ResourceNotFoundException;
import az.developia.flight_booking_name.repository.FlightRepository;
import az.developia.flight_booking_name.request.CreateFlightRequest;
import az.developia.flight_booking_name.request.UpdateFlightRequest;
import az.developia.flight_booking_name.response.FlightResponse;
import az.developia.flight_booking_name.response.UserResponse;
import az.developia.flight_booking_name.response.PlaneResponse;
import az.developia.flight_booking_name.response.SeatResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class FlightService {

    private FlightRepository flightRepository;
    private PlaneService planeService;
    private UserService userService;

    public Flight createFlight(CreateFlightRequest request, Long airlineManagerId) {
        // Validate date constraints
        if (request.getDepartureTime().isAfter(request.getArrivalTime())) {
            throw new BadRequestException("Departure time cannot be after arrival time");
        }

        if (request.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Departure time cannot be in the past");
        }

        // Check if flight number already exists
        if (flightRepository.findByFlightNumber(request.getFlightNumber()).isPresent()) {
            throw new ConflictException("Flight number already exists: " + request.getFlightNumber());
        }

        Plane plane = planeService.getPlaneById(request.getPlaneId());
        User airlineManager = userService.getUserById(airlineManagerId);

        Flight flight = Flight.builder()
                .flightNumber(request.getFlightNumber())
                .origin(request.getOrigin())
                .destination(request.getDestination())
                .departureTime(request.getDepartureTime())
                .arrivalTime(request.getArrivalTime())
                .price(request.getPrice())
                .plane(plane)
                .airlineManager(airlineManager)
                .active(true)
                .build();

        return flightRepository.save(flight);
    }

    public Flight updateFlight(Long flightId, UpdateFlightRequest request, Long airlineManagerId) {
        Flight flight = getFlightById(flightId);

        // Check if user is authorized
        if (!flight.getAirlineManager().getId().equals(airlineManagerId)) {
            throw new BadRequestException("You are not authorized to update this flight");
        }

        if (request.getDepartureTime().isAfter(request.getArrivalTime())) {
            throw new BadRequestException("Departure time cannot be after arrival time");
        }

        flight.setOrigin(request.getOrigin());
        flight.setDestination(request.getDestination());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        flight.setPrice(request.getPrice());

        return flightRepository.save(flight);
    }

    public void deleteFlight(Long flightId, Long airlineManagerId) {
        Flight flight = getFlightById(flightId);

        if (!flight.getAirlineManager().getId().equals(airlineManagerId)) {
            throw new BadRequestException("You are not authorized to delete this flight");
        }

        flight.setActive(false);
        flightRepository.save(flight);
    }

    public Flight getFlightById(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + id));
    }

    public Page<FlightResponse> getAllFlights(Pageable pageable) {
        Page<Flight> flights = flightRepository.findAll(pageable);
        List<FlightResponse> responses = flights.getContent().stream()
                .map(this::mapToFlightResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, flights.getTotalElements());
    }

    public Page<FlightResponse> searchFlights(String origin, String destination, Pageable pageable) {
        Page<Flight> flights = flightRepository.searchByOriginAndDestination(origin, destination, pageable);
        List<FlightResponse> responses = flights.getContent().stream()
                .map(this::mapToFlightResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, flights.getTotalElements());
    }

    public Page<FlightResponse> searchFlightsByDateRange(String origin, String destination,
                                                         LocalDateTime date, Pageable pageable) {
        Page<Flight> flights = flightRepository.searchByOriginDestinationAndDate(origin, destination, date, pageable);
        List<FlightResponse> responses = flights.getContent().stream()
                .map(this::mapToFlightResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, flights.getTotalElements());
    }

    public Page<FlightResponse> searchFlightsByPrice(String origin, String destination,
                                                     BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Page<Flight> flights = flightRepository.searchByOriginDestinationAndPrice(
                origin, destination, minPrice, maxPrice, pageable);
        List<FlightResponse> responses = flights.getContent().stream()
                .map(this::mapToFlightResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, flights.getTotalElements());
    }

    public Page<FlightResponse> getFlightsByAirlineManager(Long airlineManagerId, Pageable pageable) {
        Page<Flight> flights = flightRepository.findByAirlineManagerId(airlineManagerId, pageable);
        List<FlightResponse> responses = flights.getContent().stream()
                .map(this::mapToFlightResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, flights.getTotalElements());
    }

    private FlightResponse mapToFlightResponse(Flight flight) {
        UserResponse managerResponse = UserResponse.builder()
                .id(flight.getAirlineManager().getId())
                .username(flight.getAirlineManager().getUsername())
                .email(flight.getAirlineManager().getEmail())
                .fullName(flight.getAirlineManager().getFullName())
                .role(flight.getAirlineManager().getRole().toString())
                .build();

        List<SeatResponse> seatResponses = flight.getPlane().getSeats().stream()
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
                .id(flight.getPlane().getId())
                .name(flight.getPlane().getName())
                .totalSeats(flight.getPlane().getTotalSeats())
                .totalBusinessSeats(flight.getPlane().getTotalBusinessSeats())
                .totalEconomySeats(flight.getPlane().getTotalEconomySeats())
                .seats(seatResponses)
                .createdAt(flight.getPlane().getCreatedAt())
                .updatedAt(flight.getPlane().getUpdatedAt())
                .build();

        return FlightResponse.builder()
                .id(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .origin(flight.getOrigin())
                .destination(flight.getDestination())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .price(flight.getPrice())
                .plane(planeResponse)
                .airlineManager(managerResponse)
                .active(flight.getActive())
                .createdAt(flight.getCreatedAt())
                .updatedAt(flight.getUpdatedAt())
                .build();
    }
}
