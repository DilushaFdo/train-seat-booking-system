package com.example.train_seat_booking_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatAvailabilityResponse {
    private Long seatId;
    private String coachNumber;
    private String seatNumber;
    private BigDecimal fare;
    private boolean available;
}
