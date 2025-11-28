package com.example.travel_booking.dto;

import lombok.Data;

@Data
public class BookingRequest {

    private Long listingId;
    private String startDate;
    private String endDate;
}

