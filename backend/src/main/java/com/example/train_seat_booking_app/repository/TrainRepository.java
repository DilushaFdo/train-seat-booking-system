package com.example.train_seat_booking_app.repository;

import com.example.train_seat_booking_app.models.Train;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainRepository extends JpaRepository<Train, Long> {
}
