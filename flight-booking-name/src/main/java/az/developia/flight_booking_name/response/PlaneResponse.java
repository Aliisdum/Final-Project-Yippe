package az.developia.flight_booking_name.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaneResponse {
    private Long id;
    private String name;
    private Integer totalSeats;
    private Integer totalBusinessSeats;
    private Integer totalEconomySeats;
    private List<SeatResponse> seats;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
