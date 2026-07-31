package com.example.train_seat_booking_app.service;

import com.example.train_seat_booking_app.models.Booking;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SegmentOverlapChecker {
    public boolean overlaps(int aStart, int aEnd, int bStart, int bEnd) {
        return aStart < bEnd && bStart < aEnd;
    }

    public boolean hasConflict(int requestedStart, int requestedEnd, List<Booking> existingBookings) {
        return existingBookings.stream()
                .anyMatch(b -> overlaps(requestedStart, requestedEnd, b.getOriginSeq(), b.getDestinationSeq()));
    }
}
