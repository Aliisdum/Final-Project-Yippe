package az.developia.flight_booking_name.controller;

import az.developia.flight_booking_name.request.CreatePlaneRequest;
import az.developia.flight_booking_name.response.ApiResponse;
import az.developia.flight_booking_name.response.ApiResponse.PaginationInfo;
import az.developia.flight_booking_name.response.PlaneResponse;
import az.developia.flight_booking_name.service.PlaneService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planes")
@AllArgsConstructor
@Tag(name = "Plane Management", description = "Plane and Seat Management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class PlaneController {

    private PlaneService planeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a new plane", description = "Add a new aircraft to the system (Admin only)")
    public ResponseEntity<ApiResponse<PlaneResponse>> addPlane(@Valid @RequestBody CreatePlaneRequest request) {
        var plane = planeService.addPlane(request);
        var response = planeService.getAllPlanes(PageRequest.of(0, 1)).getContent().stream()
                .filter(p -> p.getId().equals(plane.getId()))
                .findFirst()
                .orElse(null);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<PlaneResponse>builder()
                        .success(true)
                        .message("Plane added successfully")
                        .data(response)
                        .statusCode(HttpStatus.CREATED.value())
                        .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get plane details", description = "Get detailed information about a specific plane")
    public ResponseEntity<ApiResponse<PlaneResponse>> getPlane(@PathVariable Long id) {
        var plane = planeService.getPlaneById(id);
        Page<PlaneResponse> page = planeService.getAllPlanes(PageRequest.of(0, 1));
        var response = page.getContent().stream()
                .filter(p -> p.getId().equals(plane.getId()))
                .findFirst()
                .orElse(null);

        return ResponseEntity.ok(ApiResponse.<PlaneResponse>builder()
                .success(true)
                .message("Plane retrieved successfully")
                .data(response)
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @GetMapping
    @Operation(summary = "Get all planes", description = "Get list of all planes with pagination")
    public ResponseEntity<ApiResponse<Page<PlaneResponse>>> getAllPlanes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PlaneResponse> planes = planeService.getAllPlanes(pageable);

        return ResponseEntity.ok(ApiResponse.<Page<PlaneResponse>>builder()
                .success(true)
                .message("Planes retrieved successfully")
                .data(planes)
                .statusCode(HttpStatus.OK.value())
                .pagination(PaginationInfo.from(planes))
                .build());
    }

    @GetMapping("/all")
    @Operation(summary = "Get all planes (simple list)", description = "Get all planes without pagination as a simple list")
    public ResponseEntity<ApiResponse<List<PlaneResponse>>> getAllPlanesAsList() {
        List<PlaneResponse> planes = planeService.getAllPlanesAsList();

        return ResponseEntity.ok(ApiResponse.<List<PlaneResponse>>builder()
                .success(true)
                .message("All planes retrieved successfully")
                .data(planes)
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a plane", description = "Delete an aircraft from the system (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deletePlane(@PathVariable Long id) {
        planeService.deletePlane(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Plane deleted successfully")
                .statusCode(HttpStatus.OK.value())
                .build());
    }
}
