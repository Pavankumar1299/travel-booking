package com.example.travel_booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.example.travel_booking")
@EnableJpaRepositories(basePackages = "com.example.travel_booking.repository")
@EntityScan(basePackages = "com.example.travel_booking.entity")
public class TravelBookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(TravelBookingApplication.class, args);
	}

}
