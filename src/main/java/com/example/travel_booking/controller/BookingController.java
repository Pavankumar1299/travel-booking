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
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    public Booking createBooking(@RequestHeader("Authorization") String auth,
                                @RequestParam Long listingId,
                                @RequestParam String start,
                                @RequestParam String end) {

        String token = auth.substring(7);
        String email = jwtUserUtil.extractEmail(token);

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Listing listing = listingRepo.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setListing(listing);

        // FIXED DATE PARSING 🔥
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);

        booking.setStartDate(startDate);
        booking.setEndDate(endDate);

        // PRICE CALCULATION 🔥
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) days = 1;

        double price = listing.getPrice() * days;
        booking.setTotalPrice(price);

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

    @DeleteMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id,
                                @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUserUtil.extractEmail(token);

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking booking = bookingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Check if booking belongs to the user
        if (!booking.getUser().getId().equals(user.getId())) {
            return "You can cancel only your own bookings!";
        }

        booking.setStatus("CANCELLED");
        bookingRepo.save(booking);

        return "Booking cancelled successfully";
    }

    @PutMapping("/update-dates/{id}")
    public Booking updateDates(@PathVariable Long id,
                            @RequestParam String startDate,
                            @RequestParam String endDate,
                            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUserUtil.extractEmail(token);

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking booking = bookingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can update only your own bookings!");
        }

        booking.setStartDate(LocalDate.parse(startDate));
        booking.setEndDate(LocalDate.parse(endDate));

        booking.setStatus("UPDATED");

        return bookingRepo.save(booking);
    }

    @GetMapping("/receipt/{id}")
    public Map<String, Object> getReceipt(@PathVariable Long id,
                                        @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUserUtil.extractEmail(token);

        

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking booking = bookingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        Map<String, Object> receipt = new LinkedHashMap<>();

        receipt.put("Booking ID", booking.getId());
        receipt.put("User", user.getEmail());
        receipt.put("Listing", booking.getListing().getTitle());
        receipt.put("Start Date", booking.getStartDate());
        receipt.put("End Date", booking.getEndDate());
        receipt.put("Total Price", booking.getTotalPrice());
        receipt.put("Status", booking.getStatus());

        return receipt;
    }


}

