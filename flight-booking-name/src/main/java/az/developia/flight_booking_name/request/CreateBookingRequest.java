package az.developia.flight_booking_name.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookingRequest {
    @NotNull(message = "Flight ID is required")
    private Long flightId;

    @NotNull(message = "Seat ID is required")
    private Long seatId;

    @NotNull(message = "Passenger name is required")
    private String passengerName;
}
