package com.example.train_seat_booking_app.controller;

import com.example.train_seat_booking_app.dto.TripSearchResponse;
import com.example.train_seat_booking_app.service.TripSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {
    private final TripSearchService tripSearchService;

    public TripController(TripSearchService tripSearchService) {
        this.tripSearchService = tripSearchService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<TripSearchResponse>> search(
            @RequestParam Long from,
            @RequestParam Long to,
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(tripSearchService.searchTrips(from, to, date));
    }

}
