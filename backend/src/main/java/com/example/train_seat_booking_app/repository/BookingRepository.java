package com.example.train_seat_booking_app.repository;

import com.example.train_seat_booking_app.models.Booking;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.trip.id = :tripId AND b.status = 'CONFIRMED'")
    List<Booking> findActiveBookingsForTrip(@Param("tripId") Long tripId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.seat.id = :seatId AND b.trip.id = :tripId AND b.status = 'CONFIRMED'")
    List<Booking> findActiveBookingsForSeatAndTripForUpdate(@Param("seatId") Long id, @Param("tripId") Long id1);
}
