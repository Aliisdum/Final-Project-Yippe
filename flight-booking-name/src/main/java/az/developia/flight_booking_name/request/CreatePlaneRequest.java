package az.developia.flight_booking_name.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePlaneRequest {
    @NotBlank(message = "Plane name is required")
    private String name;

    @NotNull(message = "Total seats is required")
    private Integer totalSeats;

    @NotNull(message = "Total business seats is required")
    private Integer totalBusinessSeats;

    @NotNull(message = "Total economy seats is required")
    private Integer totalEconomySeats;
}
