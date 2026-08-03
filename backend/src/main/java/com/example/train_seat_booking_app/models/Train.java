package com.example.train_seat_booking_app.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@Entity
@Table(name = "trains")
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy="train",
            cascade=CascadeType.ALL,
            orphanRemoval=true)
    private List<TrainRoute> routes = new ArrayList<>();

    public void addRoute(TrainRoute route) {
        routes.add(route);
        route.setTrain(this);
    }
}
