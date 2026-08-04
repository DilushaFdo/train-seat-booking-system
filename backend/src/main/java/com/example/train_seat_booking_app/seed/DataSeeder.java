package com.example.train_seat_booking_app.seed;

import com.example.train_seat_booking_app.enums.CoachType;
import com.example.train_seat_booking_app.enums.TrainDirection;
import com.example.train_seat_booking_app.enums.TripStatus;
import com.example.train_seat_booking_app.repository.*;
import com.example.train_seat_booking_app.models.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private record TrainSchedule(LocalTime departure, LocalTime arrival) {}

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
            station.setDistanceKm(new BigDecimal(row[2]));
            stationRepository.save(station);
        }

        Map<String, Station> stationsByName = new HashMap<>();
        for (Station s : stationRepository.findAll()) {
            stationsByName.put(s.getName(), s);
        }

        List<String> fullRouteStops = List.of(
                "Colombo Fort",
                "Ragama",
                "Gampaha",
                "Veyangoda",
                "Polgahawela",
                "Rambukkana",
                "Peradeniya",
                "Kandy",
                "Gampola",
                "Nawalapitiya",
                "Hatton",
                "Talawakelle",
                "Nanu Oya",
                "Haputale",
                "Bandarawela",
                "Ella",
                "Badulla"
        );

        // --- Train 1: Udarata Menike — stops at every station ---
        Train fullRouteTrain = new Train();
        fullRouteTrain.setName("Udarata Menike");
        fullRouteTrain.setDirection(TrainDirection.UP);

        int order = 1;

        for (String stationName : fullRouteStops) {
            TrainRoute route = new TrainRoute();
            route.setTrain(fullRouteTrain);
            route.setStation(stationsByName.get(stationName));
            route.setStopOrder(order++);

            fullRouteTrain.getRoutes().add(route);
        }
        trainRepository.save(fullRouteTrain);

        // --- Train 2: Podi Menike — Colombo Fort to Kandy only, skips Gampaha ---
        Train shortRouteTrain = new Train();
        shortRouteTrain.setName("Podi Menike");
        shortRouteTrain.setDirection(TrainDirection.UP);
        List<String> shortRouteStops = List.of(
                "Colombo Fort", "Ragama", "Veyangoda", "Polgahawela",
                "Rambukkana", "Peradeniya", "Kandy"
                // Gampaha deliberately excluded
        );

        order = 1;

        for (String name : shortRouteStops) {
            TrainRoute route = new TrainRoute();
            route.setTrain(shortRouteTrain);
            route.setStation(stationsByName.get(name));
            route.setStopOrder(order++);

            shortRouteTrain.getRoutes().add(route);
        }

        trainRepository.save(shortRouteTrain);

        // --- Train 3: Udarata Menike (Down) — Badulla to Colombo Fort ---
        Train reverseRouteTrain = new Train();
        reverseRouteTrain.setName("Udarata Menike (Down)");
        reverseRouteTrain.setDirection(TrainDirection.DOWN);

        List<String> reverseRouteStops = List.of(
                "Badulla",
                "Ella",
                "Bandarawela",
                "Haputale",
                "Nanu Oya",
                "Talawakelle",
                "Hatton",
                "Nawalapitiya",
                "Gampola",
                "Kandy",
                "Peradeniya",
                "Rambukkana",
                "Polgahawela",
                "Veyangoda",
                "Gampaha",
                "Ragama",
                "Colombo Fort"
        );

        order = 1;

        for (String name : reverseRouteStops) {
            TrainRoute route = new TrainRoute();
            route.setTrain(reverseRouteTrain);
            route.setStation(stationsByName.get(name));
            route.setStopOrder(order++);
            reverseRouteTrain.getRoutes().add(route);
        }
        trainRepository.save(reverseRouteTrain);

        // --- Coaches, Seats, Trips — per train ---
        List<Train> allTrains = List.of(fullRouteTrain, shortRouteTrain, reverseRouteTrain);
        LocalDate tripDate = LocalDate.of(2026, 8, 5);

        for (Train train : allTrains) {

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
            }

            // --- Trips: two scheduled services per train (morning + evening) ---
            for (TrainSchedule schedule : getSchedulesFor(train.getName())) {
                Trip trip = new Trip();
                trip.setTrain(train);
                trip.setTripDate(tripDate);
                trip.setDepartureTime(schedule.departure());
                trip.setArrivalTime(schedule.arrival());
                trip.setStatus(TripStatus.SCHEDULED);
                tripRepository.save(trip);
            }
        }
    }

    private List<TrainSchedule> getSchedulesFor(String trainName) {
        return switch (trainName) {
            case "Udarata Menike" -> List.of(
                    new TrainSchedule(LocalTime.of(5, 55), LocalTime.of(15, 15)),
                    new TrainSchedule(LocalTime.of(14, 35), LocalTime.of(23, 55))
            );
            case "Podi Menike" -> List.of(
                    new TrainSchedule(LocalTime.of(7, 30), LocalTime.of(11, 10)),
                    new TrainSchedule(LocalTime.of(15, 35), LocalTime.of(19, 15))
            );
            case "Udarata Menike (Down)" -> List.of(
                    new TrainSchedule(LocalTime.of(8, 47), LocalTime.of(17, 40)),
                    new TrainSchedule(LocalTime.of(21, 25), LocalTime.of(6, 20))
            );
            default -> List.of(
                    new TrainSchedule(LocalTime.of(6, 0), LocalTime.of(14, 0))
            );
        };
    }
}