package com.example.train_seat_booking_app.controller;
import com.example.train_seat_booking_app.models.Station;
import com.example.train_seat_booking_app.repository.StationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    private  StationRepository stationRepository;

    public StationController(StationRepository stationRepository){
        this.stationRepository = stationRepository;
    }

    @GetMapping
    public ResponseEntity<List<Station>> getAllStations() {
        List<Station> stations = stationRepository.findAll()
                .stream()
                .sorted(Comparator.comparingInt(Station::getSequenceOrder))
                .collect(Collectors.toList());
        return ResponseEntity.ok(stations);
    }
}
