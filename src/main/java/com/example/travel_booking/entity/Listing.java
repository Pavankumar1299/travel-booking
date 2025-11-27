package com.example.travel_booking.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "listings")
@Data
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;     // HOTEL, FLIGHT, CAR
    private String title;
    private String description;
    private String location;
    private Double price;

    @ElementCollection
    private List<String> imageUrls;
}

