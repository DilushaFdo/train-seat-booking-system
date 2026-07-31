package com.example.train_seat_booking_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {
    private Long tripId;
    private Long seatId;
    private Long fromStationId;
    private Long toStationId;
    private String passengerName;

}
