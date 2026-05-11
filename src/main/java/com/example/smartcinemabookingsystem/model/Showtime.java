package com.example.smartcinemabookingsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "showtimes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Showtime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    private double price;

    // Trường mới để cho phép tùy chỉnh số ghế cho suất chiếu
    // Sử dụng Integer để có thể là null nếu không muốn tùy chỉnh
    private Integer customTotalSeats;

    @Transient
    private boolean soldOut;

    @Transient
    private boolean started;

    @Transient
    private String statusLabel;

    public Showtime(Long id, Movie movie, Room room, LocalDateTime startTime, LocalDateTime endTime,
                    double price, Integer customTotalSeats) {
        this.id = id;
        this.movie = movie;
        this.room = room;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.customTotalSeats = customTotalSeats;
    }
}
