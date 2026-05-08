package com.example.smartcinemabookingsystem.repository;

import com.example.smartcinemabookingsystem.model.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    List<Showtime> findByMovieId(Long movieId);
    List<Showtime> findByRoomIdAndStartTimeBetween(Long roomId, LocalDateTime start, LocalDateTime end);
    List<Showtime> findByRoomId(Long roomId); // Added for easier conflict checking

    // New method for CORE-08: Find showtimes that have not yet started
    List<Showtime> findByStartTimeAfter(LocalDateTime currentTime);
}
