package com.example.travel_booking.repository;

import com.example.travel_booking.entity.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    List<Listing> findByTitleContainingIgnoreCase(String title);
    List<Listing> findByLocationContainingIgnoreCase(String location);
}
