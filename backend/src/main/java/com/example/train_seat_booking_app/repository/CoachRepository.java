package com.example.train_seat_booking_app.repository;

import com.example.train_seat_booking_app.models.Coach;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoachRepository extends JpaRepository<Coach, Long> {
}
