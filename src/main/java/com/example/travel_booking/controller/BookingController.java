package com.example.travel_booking.controller;

import com.example.travel_booking.dto.BookingRequest;
import com.example.travel_booking.entity.Booking;
import com.example.travel_booking.entity.Listing;
import com.example.travel_booking.entity.User;
import com.example.travel_booking.repository.BookingRepository;
import com.example.travel_booking.repository.ListingRepository;
import com.example.travel_booking.repository.UserRepository;
import com.example.travel_booking.security.JwtUserUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private ListingRepository listingRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private JwtUserUtil jwtUserUtil;

    // CREATE BOOKING
    @PostMapping("/create")
    public Booking createBooking(@RequestBody BookingRequest request,
                                 @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUserUtil.extractEmail(token);

        User user = userRepo.findByEmail(email).orElseThrow();

        Listing listing = listingRepo.findById(request.getListingId())
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        Booking booking = new Booking();
        booking.setUserId(user.getId());
        booking.setListingId(listing.getId());

        LocalDate start = LocalDate.parse(request.getStartDate());
        LocalDate end = LocalDate.parse(request.getEndDate());

        booking.setStartDate(start);
        booking.setEndDate(end);

        long days = end.toEpochDay() - start.toEpochDay();
        if (days <= 0) days = 1;

        booking.setTotalPrice(days * listing.getPrice());
        booking.setStatus("PENDING");

        return bookingRepo.save(booking);
    }

    // USER BOOKING HISTORY
    @GetMapping("/history")
    public java.util.List<Booking> history(@RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUserUtil.extractEmail(token);

        User user = userRepo.findByEmail(email).orElseThrow();

        return bookingRepo.findByUserId(user.getId());
    }

    private void validateAdmin(String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUserUtil.extractEmail(token);

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid user"));

        if (!user.getRole().equals("ADMIN")) {
            throw new RuntimeException("Unauthorized: Admin only");
        }
    }

    @GetMapping("/all")
    public List<Booking> getAllBookings(@RequestHeader("Authorization") String authHeader) {

        validateAdmin(authHeader);
        return bookingRepo.findAll();
    }

    @PutMapping("/update-status/{id}")
    public Booking updateStatus(@PathVariable Long id,
                                @RequestParam String status,
                                @RequestHeader("Authorization") String authHeader) {

        validateAdmin(authHeader);

        Booking booking = bookingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(status.toUpperCase());  // APPROVED / CANCELLED / PENDING

        return bookingRepo.save(booking);
    }
}

