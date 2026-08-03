package com.example.train_seat_booking_app.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="train_routes")
@Data
public class TrainRoute {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name="train_id", nullable=false)
        private Train train;

        @ManyToOne
        @JoinColumn(name="station_id", nullable=false)
        private Station station;

        @Column(nullable=false)
        private Integer stopOrder;
}