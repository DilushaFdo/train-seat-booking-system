package com.example.train_seat_booking_app.repository;

import com.example.train_seat_booking_app.models.Station;
import com.example.train_seat_booking_app.models.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {
    @Query("""
        SELECT t 
        FROM Trip t
        JOIN t.train.routes fromRoute
        JOIN t.train.routes toRoute
        WHERE fromRoute.station = :fromStation
        AND toRoute.station = :toStation
        AND fromRoute.stopOrder < toRoute.stopOrder
        AND t.tripDate = :date
        AND t.status = 'SCHEDULED'
        """)
    List<Trip> findTripsStoppingAtBoth(
            @Param("fromStation") Station fromStation,
            @Param("toStation") Station toStation,
            @Param("date") LocalDate date
    );
    }

