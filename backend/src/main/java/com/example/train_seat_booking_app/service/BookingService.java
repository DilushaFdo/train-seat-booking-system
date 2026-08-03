package com.example.train_seat_booking_app.service;
import com.example.train_seat_booking_app.dto.BookingRequest;
import com.example.train_seat_booking_app.dto.BookingResponse;
import com.example.train_seat_booking_app.enums.TripStatus;
import com.example.train_seat_booking_app.exception.SeatUnavailableException;
import com.example.train_seat_booking_app.models.*;
import com.example.train_seat_booking_app.repository.*;
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

    private TrainRouteRepository trainRouteRepository;
    private FareCalculator fareCalculator;
    private SegmentOverlapChecker checker;

    public BookingService(BookingRepository bookingRepository, SeatRepository seatRepository,
                          TripRepository tripRepository, StationRepository stationRepository,
                          TrainRouteRepository trainRouteRepository,
                          FareCalculator fareCalculator, SegmentOverlapChecker checker) {
        this.bookingRepository = bookingRepository;
        this.seatRepository = seatRepository;
        this.tripRepository = tripRepository;
        this.stationRepository = stationRepository;
        this.trainRouteRepository = trainRouteRepository;
        this.fareCalculator = fareCalculator;
        this.checker = checker;
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));

        // Lock the seat row itself first
        // concurrent booking attempts, even when no bookings exist yet.
        Seat seat = seatRepository.findByIdForUpdate(request.getSeatId())
                .orElseThrow(() -> new IllegalArgumentException("Seat not found"));

        Station origin = stationRepository.findById(request.getFromStationId())
                .orElseThrow(() -> new IllegalArgumentException("Origin station not found"));
        Station destination = stationRepository.findById(request.getToStationId())
                .orElseThrow(() -> new IllegalArgumentException("Destination station not found"));

        TrainRoute originRoute =
                trainRouteRepository
                        .findByTrainAndStation(trip.getTrain(), origin)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Origin station not in train route"
                                )
                        );

        TrainRoute destinationRoute =
                trainRouteRepository
                        .findByTrainAndStation(trip.getTrain(), destination)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Destination station not in train route"
                                )
                        );

        if (trip.getStatus() != TripStatus.SCHEDULED) {
            throw new IllegalArgumentException("This trip is not available for booking (status: " + trip.getStatus() + ").");
        }

        int originSeq = originRoute.getStopOrder();
        int destSeq = destinationRoute.getStopOrder();

        if (destSeq <= originSeq) {
            throw new IllegalArgumentException("Destination must be after origin");
        }

        // Now safe to check — we're holding the seat's lock, so no other
        // transaction can be doing this same check-and-insert concurrently.
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
