package az.developia.flight_booking_name.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatResponse {
    private Long id;
    private String seatNumber;
    private String seatClass;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
