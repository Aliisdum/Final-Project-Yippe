package az.developia.flight_booking_name.controller;

import az.developia.flight_booking_name.request.CreateFlightRequest;
import az.developia.flight_booking_name.request.UpdateFlightRequest;
import az.developia.flight_booking_name.response.ApiResponse;
import az.developia.flight_booking_name.response.ApiResponse.PaginationInfo;
import az.developia.flight_booking_name.response.FlightResponse;
import az.developia.flight_booking_name.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/flights")
@AllArgsConstructor
@Tag(name = "Flight Management", description = "Flight Management APIs")
public class FlightController {

    private FlightService flightService;

    private Long getAirlineManagerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // For testing purposes, using a default manager ID
        return 1L; // This should be replaced with actual user ID from token
    }

    @PostMapping
    @PreAuthorize("hasRole('AIRLINE_MANAGER') or hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create a new flight", description = "Create a new flight (Airline Manager or Admin)")
    public ResponseEntity<ApiResponse<FlightResponse>> createFlight(@Valid @RequestBody CreateFlightRequest request) {
        Long managerId = getAirlineManagerId();
        var flight = flightService.createFlight(request, managerId);
        var flightPage = flightService.getAllFlights(PageRequest.of(0, 1));
        var response = flightPage.getContent().stream()
                .filter(f -> f.getId().equals(flight.getId()))
                .findFirst()
                .orElse(null);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<FlightResponse>builder()
                        .success(true)
                        .message("Flight created successfully")
                        .data(response)
                        .statusCode(HttpStatus.CREATED.value())
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('AIRLINE_MANAGER') or hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update flight details", description = "Update an existing flight")
    public ResponseEntity<ApiResponse<FlightResponse>> updateFlight(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFlightRequest request) {
        Long managerId = getAirlineManagerId();
        var flight = flightService.updateFlight(id, request, managerId);
        var flightPage = flightService.getAllFlights(PageRequest.of(0, 1));
        var response = flightPage.getContent().stream()
                .filter(f -> f.getId().equals(flight.getId()))
                .findFirst()
                .orElse(null);

        return ResponseEntity.ok(ApiResponse.<FlightResponse>builder()
                .success(true)
                .message("Flight updated successfully")
                .data(response)
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('AIRLINE_MANAGER') or hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete a flight", description = "Delete (deactivate) a flight")
    public ResponseEntity<ApiResponse<?>> deleteFlight(@PathVariable Long id) {
        Long managerId = getAirlineManagerId();
        flightService.deleteFlight(id, managerId);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Flight deleted successfully")
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get flight details", description = "Get detailed information about a specific flight")
    public ResponseEntity<ApiResponse<FlightResponse>> getFlightById(@PathVariable Long id) {
        var flight = flightService.getFlightById(id);
        var flightPage = flightService.getAllFlights(PageRequest.of(0, 1));
        var response = flightPage.getContent().stream()
                .filter(f -> f.getId().equals(flight.getId()))
                .findFirst()
                .orElse(null);

        return ResponseEntity.ok(ApiResponse.<FlightResponse>builder()
                .success(true)
                .message("Flight retrieved successfully")
                .data(response)
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @GetMapping
    @Operation(summary = "Get all flights", description = "Get list of all flights with pagination")
    public ResponseEntity<ApiResponse<Page<FlightResponse>>> getAllFlights(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FlightResponse> flights = flightService.getAllFlights(pageable);

        return ResponseEntity.ok(ApiResponse.<Page<FlightResponse>>builder()
                .success(true)
                .message("Flights retrieved successfully")
                .data(flights)
                .statusCode(HttpStatus.OK.value())
                .pagination(PaginationInfo.from(flights))
                .build());
    }

    @GetMapping("/search/route")
    @Operation(summary = "Search flights by route", description = "Search flights by origin and destination")
    public ResponseEntity<ApiResponse<Page<FlightResponse>>> searchFlightsByRoute(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FlightResponse> flights = flightService.searchFlights(origin, destination, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<FlightResponse>>builder()
                .success(true)
                .message("Flights found")
                .data(flights)
                .statusCode(HttpStatus.OK.value())
                .pagination(PaginationInfo.from(flights))
                .build());
    }

    @GetMapping("/search/price")
    @Operation(summary = "Search flights by price range", description = "Search flights by origin, destination, and price")
    public ResponseEntity<ApiResponse<Page<FlightResponse>>> searchFlightsByPrice(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FlightResponse> flights = flightService.searchFlightsByPrice(
                origin, destination, minPrice, maxPrice, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<FlightResponse>>builder()
                .success(true)
                .message("Flights found")
                .data(flights)
                .statusCode(HttpStatus.OK.value())
                .pagination(PaginationInfo.from(flights))
                .build());
    }
}
