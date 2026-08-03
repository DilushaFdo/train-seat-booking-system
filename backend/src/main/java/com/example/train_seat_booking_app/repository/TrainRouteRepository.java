package com.example.train_seat_booking_app.repository;

import com.example.train_seat_booking_app.models.Station;
import com.example.train_seat_booking_app.models.Train;
import com.example.train_seat_booking_app.models.TrainRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainRouteRepository  extends JpaRepository<TrainRoute,Long> {

    Optional<TrainRoute> findByTrainAndStation(Train train, Station station);
}
