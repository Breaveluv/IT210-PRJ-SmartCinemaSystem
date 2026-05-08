package com.example.smartcinemabookingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingHistoryDTO {
    private Long bookingId;
    private LocalDateTime bookingTime;
    private BigDecimal totalAmount;
    private String bookingStatus;

    private Long showtimeId;
    private LocalDateTime showtimeStartTime;
    private BigDecimal showtimePrice;

    private Long movieId;
    private String movieTitle;
    private String moviePosterUrl;

    private Long roomId;
    private String roomName;

    private List<String> seatNames; // Danh sách tên ghế đã đặt
}
