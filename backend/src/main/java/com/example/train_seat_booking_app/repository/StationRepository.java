package com.example.train_seat_booking_app.repository;

import com.example.train_seat_booking_app.models.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StationRepository extends JpaRepository<Station, Long> {
    Optional<Station> findByName(String name);
}
