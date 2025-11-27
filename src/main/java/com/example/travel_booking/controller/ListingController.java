package com.example.travel_booking.controller;


import com.example.travel_booking.entity.Listing;
import com.example.travel_booking.repository.ListingRepository;
import com.example.travel_booking.service.S3Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/listing")
public class ListingController {

    @Autowired
    private ListingRepository listingRepo;

    @Autowired
    private S3Service s3Service;

    private final ObjectMapper mapper = new ObjectMapper();

    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public Listing createListing(
            @RequestPart("data") String data,
            @RequestPart("images") MultipartFile[] images
    ) throws IOException {

        // Convert JSON string to Listing object
        Listing listing = mapper.readValue(data, Listing.class);

        List<String> imageUrls = new ArrayList<>();

        // Upload files to S3
        for (MultipartFile image : images) {
            String url = s3Service.uploadFile(image);
            imageUrls.add(url);
        }

        listing.setImageUrls(imageUrls);

        return listingRepo.save(listing);
    }


    // SEARCH LISTINGS
    @GetMapping("/search")
    public List<Listing> search(@RequestParam String keyword) {
        List<Listing> byTitle = listingRepo.findByTitleContainingIgnoreCase(keyword);
        List<Listing> byLocation = listingRepo.findByLocationContainingIgnoreCase(keyword);

        // Merge results
        byLocation.forEach(listing -> {
            if (!byTitle.contains(listing)) byTitle.add(listing);
        });

        return byTitle;
    }
}

