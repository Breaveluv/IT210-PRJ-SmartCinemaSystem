package com.example.smartcinemabookingsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String seatName;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
}
