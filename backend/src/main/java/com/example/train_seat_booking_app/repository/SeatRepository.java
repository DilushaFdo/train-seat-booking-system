package com.example.train_seat_booking_app.repository;

import com.example.train_seat_booking_app.models.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Query("""
        SELECT s 
        FROM Seat s
        JOIN s.coach c
        WHERE c.type = 'RESERVED'
        AND c.train.id = :trainId
        """)
    List<Seat> findAllReservedSeatsByTrain(@Param("trainId") Integer trainId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id = :seatId")
    Optional<Seat> findByIdForUpdate(@Param("seatId") Long seatId);
}
