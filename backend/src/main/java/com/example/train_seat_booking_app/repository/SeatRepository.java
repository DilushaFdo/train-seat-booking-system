package com.example.train_seat_booking_app.repository;

import com.example.train_seat_booking_app.models.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    @Query("SELECT s FROM Seat s WHERE s.coach.type = 'RESERVED'")
    List<Seat> findAllReservedSeats();
}
