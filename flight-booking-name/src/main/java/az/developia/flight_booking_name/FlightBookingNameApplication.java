package az.developia.flight_booking_name;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "az.developia.flight_booking_name")
public class FlightBookingNameApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlightBookingNameApplication.class, args);
	}

}
