package com.example.train_seat_booking_app.controller;

import com.example.train_seat_booking_app.dto.SeatAvailabilityResponse;
import com.example.train_seat_booking_app.service.AvailabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService){
        this.availabilityService = availabilityService;
    }

    @GetMapping("/{tripId}/availability")
    public ResponseEntity<List<SeatAvailabilityResponse>> getAvailability(
            @PathVariable Long tripId,
            @RequestParam Long from,
            @RequestParam Long to
    ){
        return ResponseEntity.ok( availabilityService.findAvailableSeats( tripId, from, to));
    }
}
