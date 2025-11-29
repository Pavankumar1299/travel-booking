package com.example.travel_booking.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // private Long userId; 
    // private Long listingId;
    
    private LocalDate startDate;
    private LocalDate endDate;

    private Double totalPrice;

    private String status = "PENDING";  // default

    private LocalDateTime createdAt = LocalDateTime.now();

    // RELATION WITH USER
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // RELATION WITH LISTING
    @ManyToOne
    @JoinColumn(name = "listing_id")
    private Listing listing;
}
