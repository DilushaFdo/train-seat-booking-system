package com.example.train_seat_booking_app.repository;

import com.example.train_seat_booking_app.models.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trip, Long> {
}
