package com.example.train_seat_booking_app.service;
import com.example.train_seat_booking_app.dto.BookingRequest;
import com.example.train_seat_booking_app.dto.BookingResponse;
import com.example.train_seat_booking_app.exception.SeatUnavailableException;
import com.example.train_seat_booking_app.models.Booking;
import com.example.train_seat_booking_app.models.Seat;
import com.example.train_seat_booking_app.models.Station;
import com.example.train_seat_booking_app.models.Trip;
import com.example.train_seat_booking_app.repository.BookingRepository;
import com.example.train_seat_booking_app.repository.SeatRepository;
import com.example.train_seat_booking_app.repository.StationRepository;
import com.example.train_seat_booking_app.repository.TripRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private TripRepository tripRepository;
    private StationRepository stationRepository;
    private SeatRepository seatRepository;
    private BookingRepository bookingRepository;
    private FareCalculator fareCalculator;
    private SegmentOverlapChecker checker;

    public BookingService(BookingRepository bookingRepository, SeatRepository seatRepository,
                          TripRepository tripRepository, StationRepository stationRepository,
                          FareCalculator fareCalculator, SegmentOverlapChecker checker) {
        this.bookingRepository = bookingRepository;
        this.seatRepository = seatRepository;
        this.tripRepository = tripRepository;
        this.stationRepository = stationRepository;
        this.fareCalculator = fareCalculator;
        this.checker = checker;
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));
        Seat seat = seatRepository.findById(request.getSeatId())
                .orElseThrow(() -> new IllegalArgumentException("Seat not found"));
        Station origin = stationRepository.findById(request.getFromStationId())
                .orElseThrow(() -> new IllegalArgumentException("Origin station not found"));
        Station destination = stationRepository.findById(request.getToStationId())
                .orElseThrow(() -> new IllegalArgumentException("Destination station not found"));

        int originSeq = origin.getSequenceOrder();
        int destSeq = destination.getSequenceOrder();

        if (destSeq <= originSeq) {
            throw new IllegalArgumentException("Destination must be after origin");
        }

        // Locking query — blocks concurrent transactions for this exact seat+trip
        List<Booking> existing = bookingRepository.findActiveBookingsForSeatAndTripForUpdate(seat.getId(), trip.getId());

        boolean conflict = checker.hasConflict(originSeq, destSeq, existing);

        if (conflict) {
            throw new SeatUnavailableException("This seat is already booked for an overlapping segment.");
        }

        BigDecimal fare = fareCalculator.calculateFare(origin, destination);

        Booking booking = new Booking();
        booking.setTrip(trip);
        booking.setSeat(seat);
        booking.setOriginStation(origin);
        booking.setDestinationStation(destination);
        booking.setOriginSeq(originSeq);
        booking.setDestinationSeq(destSeq);
        booking.setPassengerName(request.getPassengerName());
        booking.setFare(fare);
        booking.setStatus("CONFIRMED");
        booking.setCreatedAt(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);

        return new BookingResponse(
                saved.getId(),
                saved.getPassengerName(),
                seat.getCoach().getCoachNumber(),
                seat.getSeatNumber(),
                origin.getName(),
                destination.getName(),
                fare,
                saved.getStatus()
        );
    }

}
