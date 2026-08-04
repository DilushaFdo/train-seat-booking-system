package com.example.train_seat_booking_app.service;

import com.example.train_seat_booking_app.models.Station;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FareCalculator {

    private static final BigDecimal RATE_PER_KM = new BigDecimal("2.50");
    private static final BigDecimal BASE_FARE = new BigDecimal("50.00");

    public BigDecimal calculateFare(Station origin, Station destination) {
        BigDecimal distance = destination.getDistanceKm().subtract(origin.getDistanceKm()).abs();
        return BASE_FARE.add(distance.multiply(RATE_PER_KM)).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
