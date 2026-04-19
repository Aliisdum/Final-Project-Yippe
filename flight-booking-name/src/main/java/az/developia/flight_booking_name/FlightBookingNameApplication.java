package az.developia.flight_booking_name;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@ComponentScan(basePackages = "az.developia.flight_booking_name")
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class FlightBookingNameApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlightBookingNameApplication.class, args);
	}

}
