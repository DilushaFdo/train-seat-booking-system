package com.example.train_seat_booking_app.seed;

import com.example.train_seat_booking_app.enums.CoachType;
import com.example.train_seat_booking_app.repository.*;
import com.example.train_seat_booking_app.models.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    private final StationRepository stationRepository;
    private final TrainRepository trainRepository;
    private final CoachRepository coachRepository;
    private final SeatRepository seatRepository;
    private final TripRepository tripRepository;

    public DataSeeder(StationRepository stationRepository,
                      TrainRepository trainRepository,
                      CoachRepository coachRepository,
                      SeatRepository seatRepository,
                      TripRepository tripRepository) {
        this.stationRepository = stationRepository;
        this.trainRepository = trainRepository;
        this.coachRepository = coachRepository;
        this.seatRepository = seatRepository;
        this.tripRepository = tripRepository;
    }

    @Override
    public void run(String... args) {
        if (stationRepository.count() > 0) {
            return; // already seeded, do nothing
        }

        // --- Stations ---
        String[][] stationData = {
                {"Colombo Fort", "0", "0"},
                {"Ragama", "1", "15"},
                {"Gampaha", "2", "30"},
                {"Veyangoda", "3", "43"},
                {"Polgahawela", "4", "66"},
                {"Rambukkana", "5", "80"},
                {"Peradeniya", "6", "110"},
                {"Kandy", "7", "121"},
                {"Gampola", "8", "138"},
                {"Nawalapitiya", "9", "155"},
                {"Hatton", "10", "195"},
                {"Talawakelle", "11", "210"},
                {"Nanu Oya", "12", "228"},
                {"Haputale", "13", "253"},
                {"Bandarawela", "14", "266"},
                {"Ella", "15", "278"},
                {"Badulla", "16", "292"}
        };

        for (String[] row : stationData) {
            Station station = new Station();
            station.setName(row[0]);
            station.setSequenceOrder(Integer.parseInt(row[1]));
            station.setDistanceKm(new BigDecimal(row[2]));
            stationRepository.save(station);
        }

        // --- Train ---
        Train train = new Train();
        train.setName("Udarata Menike");
        trainRepository.save(train);

        // --- Coaches + Seats ---
        for (int i = 1; i <= 3; i++) {
            Coach coach = new Coach();
            coach.setTrain(train);
            coach.setCoachNumber("R" + i);
            coach.setType(CoachType.RESERVED);
            coach.setSeatCount(40);
            coachRepository.save(coach);

            for (int s = 1; s <= 40; s++) {
                Seat seat = new Seat();
                seat.setCoach(coach);
                seat.setSeatNumber(String.valueOf(s));
                seatRepository.save(seat);
            }
        }

        for (int i = 1; i <= 5; i++) {
            Coach coach = new Coach();
            coach.setTrain(train);
            coach.setCoachNumber("U" + i);
            coach.setType(CoachType.UNRESERVED);
            coach.setSeatCount(80);
            coachRepository.save(coach);
            // no Seat rows — unreserved coaches don't need individual seats
        }

        // --- Trip ---
        Trip trip = new Trip();
        trip.setTrain(train);
        trip.setTripDate(LocalDate.of(2026, 8, 5));
        tripRepository.save(trip);
    }
}