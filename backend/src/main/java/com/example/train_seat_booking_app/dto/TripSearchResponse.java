package com.example.train_seat_booking_app.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TripSearchResponse {
    private Long tripId;
    private String trainName;
    private LocalDate tripDate;
    private String status;
    private LocalTime departureTime;
    private LocalTime arrivalTime;

    public TripSearchResponse(Long tripId, String trainName, LocalDate tripDate, String status, LocalTime depatureTime, LocalTime arrivalTime) {
        this.tripId = tripId;
        this.trainName = trainName;
        this.tripDate = tripDate;
        this.status = status;
        this.departureTime = depatureTime;
        this.arrivalTime = arrivalTime;
    }
}
