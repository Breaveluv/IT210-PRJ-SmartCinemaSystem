package com.example.smartcinemabookingsystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "Phim không được để trống")
    private Movie movie = new Movie();

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    @NotNull(message = "Phòng chiếu không được để trống")
    private Room room = new Room();

    @Column(nullable = false)
    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @FutureOrPresent(message = "Thời gian bắt đầu phải là hiện tại hoặc trong tương lai")
    @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startTime;

    @Column(nullable = false)
    @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endTime;

    @Min(value = 0, message = "Giá vé phải lớn hơn hoặc bằng 0")
    private double price;

    // Trường mới để cho phép tùy chỉnh số ghế cho suất chiếu
    // Sử dụng Integer để có thể là null nếu không muốn tùy chỉnh
    @Min(value = 1, message = "Số ghế tùy chỉnh phải lớn hơn 0")
    private Integer customTotalSeats;

    @Transient
    private boolean soldOut;

    @Transient
    private boolean started;

    @Transient
    private String statusLabel;

    // Constructor for JPA and Lombok
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


