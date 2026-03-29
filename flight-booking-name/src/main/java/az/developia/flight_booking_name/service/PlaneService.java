package az.developia.flight_booking_name.service;

import az.developia.flight_booking_name.entity.Plane;
import az.developia.flight_booking_name.entity.Seat;
import az.developia.flight_booking_name.entity.SeatClass;
import az.developia.flight_booking_name.exception.ConflictException;
import az.developia.flight_booking_name.exception.ResourceNotFoundException;
import az.developia.flight_booking_name.repository.PlaneRepository;
import az.developia.flight_booking_name.repository.SeatRepository;
import az.developia.flight_booking_name.request.CreatePlaneRequest;
import az.developia.flight_booking_name.response.PlaneResponse;
import az.developia.flight_booking_name.response.SeatResponse;
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
public class PlaneService {

    private PlaneRepository planeRepository;
    private SeatRepository seatRepository;

    public Plane addPlane(CreatePlaneRequest request) {
        if (planeRepository.existsByName(request.getName())) {
            throw new ConflictException("Plane with this name already exists: " + request.getName());
        }

        Plane plane = Plane.builder()
                .name(request.getName())
                .totalSeats(request.getTotalSeats())
                .totalBusinessSeats(request.getTotalBusinessSeats())
                .totalEconomySeats(request.getTotalEconomySeats())
                .build();

        Plane savedPlane = planeRepository.save(plane);
        createSeats(savedPlane, request.getTotalBusinessSeats(), request.getTotalEconomySeats());
        return savedPlane;
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

    public Plane getPlaneById(Long id) {
        return planeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plane not found with id: " + id));
    }

    public Page<PlaneResponse> getAllPlanes(Pageable pageable) {
        Page<Plane> planes = planeRepository.findAll(pageable);
        List<PlaneResponse> responses = planes.getContent().stream()
                .map(this::mapToPlaneResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, planes.getTotalElements());
    }

    public List<PlaneResponse> getAllPlanesAsList() {
        List<Plane> planes = planeRepository.findAll();
        return planes.stream()
                .map(this::mapToPlaneResponse)
                .collect(Collectors.toList());
    }

    public void deletePlane(Long id) {
        Plane plane = getPlaneById(id);
        // Delete all seats associated with this plane
        seatRepository.deleteAll(plane.getSeats());
        // Delete the plane
        planeRepository.delete(plane);
    }

    private PlaneResponse mapToPlaneResponse(Plane plane) {
        List<SeatResponse> seatResponses = plane.getSeats().stream()
                .map(seat -> SeatResponse.builder()
                        .id(seat.getId())
                        .seatNumber(seat.getSeatNumber())
                        .seatClass(seat.getSeatClass().toString())
                        .status(seat.getStatus().toString())
                        .createdAt(seat.getCreatedAt())
                        .updatedAt(seat.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return PlaneResponse.builder()
                .id(plane.getId())
                .name(plane.getName())
                .totalSeats(plane.getTotalSeats())
                .totalBusinessSeats(plane.getTotalBusinessSeats())
                .totalEconomySeats(plane.getTotalEconomySeats())
                .seats(seatResponses)
                .createdAt(plane.getCreatedAt())
                .updatedAt(plane.getUpdatedAt())
                .build();
    }
}
