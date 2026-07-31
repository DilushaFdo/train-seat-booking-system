package com.example.train_seat_booking_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long bookingId;
    private String passengerName;
    private String coachNumber;
    private String seatNumber;
    private String originStation;
    private String destinationStation;
    private BigDecimal fare;
    private String status;
}
