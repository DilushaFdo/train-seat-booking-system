package com.example.train_seat_booking_app.service;

import com.example.train_seat_booking_app.dto.TripSearchResponse;
import com.example.train_seat_booking_app.models.Station;
import com.example.train_seat_booking_app.repository.StationRepository;
import com.example.train_seat_booking_app.repository.TrainRouteRepository;
import com.example.train_seat_booking_app.repository.TripRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TripSearchService {
    private final TripRepository tripRepository;
    private final StationRepository stationRepository;

    private final TrainRouteRepository trainRouteRepository;

    public TripSearchService(TripRepository tripRepository,
                             StationRepository stationRepository,
                             TrainRouteRepository trainRouteRepository) {
        this.tripRepository = tripRepository;
        this.stationRepository = stationRepository;
        this.trainRouteRepository = trainRouteRepository;
    }

    public List<TripSearchResponse> searchTrips(Long fromStationId, Long toStationId, LocalDate date) {
        Station from = stationRepository.findById(fromStationId)
                .orElseThrow(() -> new IllegalArgumentException("Origin station not found"));
        Station to = stationRepository.findById(toStationId)
                .orElseThrow(() -> new IllegalArgumentException("Destination station not found"));

        return tripRepository.findTripsStoppingAtBoth(from, to, date)
                .stream()
                .map(trip -> new TripSearchResponse(trip.getId(),
                        trip.getTrain().getName(),
                        trip.getTripDate(),
                        trip.getStatus().name(),
                        trip.getDepartureTime(),
                        trip.getArrivalTime()))
                .collect(Collectors.toList());
    }

}
