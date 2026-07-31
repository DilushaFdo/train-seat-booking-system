package com.example.train_seat_booking_app.service;

import com.example.train_seat_booking_app.dto.SeatAvailabilityResponse;
import com.example.train_seat_booking_app.models.Booking;
import com.example.train_seat_booking_app.models.Seat;
import com.example.train_seat_booking_app.models.Station;
import com.example.train_seat_booking_app.repository.BookingRepository;
import com.example.train_seat_booking_app.repository.SeatRepository;
import com.example.train_seat_booking_app.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    private StationRepository stationRepository;
    private BookingRepository bookingRepository;
    private SeatRepository seatRepository;
    private FareCalculator fareCalculator;

    private SegmentOverlapChecker checker;

    public AvailabilityService(SeatRepository seatRepository,
                               BookingRepository bookingRepository,
                               StationRepository stationRepository,
                               FareCalculator fareCalculator, SegmentOverlapChecker checker) {
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
        this.stationRepository = stationRepository;
        this.fareCalculator = fareCalculator;
        this.checker = checker;
    }

    public List<SeatAvailabilityResponse> findAvailableSeats(Long tripId, Long from, Long to) {
        Station origin = stationRepository.findById(from)
                .orElseThrow(() -> new IllegalArgumentException("Origin station not found"));
        Station destination = stationRepository.findById(to)
                .orElseThrow(() -> new IllegalArgumentException("Destination station not found"));

        int originSeq = origin.getSequenceOrder();
        int destSeq = destination.getSequenceOrder();

        if (destSeq <= originSeq) {
            throw new IllegalArgumentException("Destination must be after origin");
        }

        List<Booking> existingBookings = bookingRepository.findActiveBookingsForTrip(tripId);
        List<Seat> allReservedSeats = seatRepository.findAllReservedSeats();

        BigDecimal fare = fareCalculator.calculateFare(origin, destination);

        List<SeatAvailabilityResponse> result  = new ArrayList<>();

        for (Seat seat : allReservedSeats) {
            List<Booking> seatBookings = existingBookings.stream()
                    .filter(b -> b.getSeat().getId().equals(seat.getId()))
                    .collect(Collectors.toList());

            boolean conflict = checker.hasConflict(originSeq, destSeq, seatBookings);

            result.add(new SeatAvailabilityResponse(
                    seat.getId(),
                    seat.getCoach().getCoachNumber(),
                    seat.getSeatNumber(),
                    fare,
                    !conflict  // available = no conflict
            ));
        }

        return result;
    }



}
