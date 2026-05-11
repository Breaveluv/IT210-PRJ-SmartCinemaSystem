package com.example.smartcinemabookingsystem.repository;

import com.example.smartcinemabookingsystem.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    @Query(
            value = "SELECT DISTINCT s.movie FROM Showtime s WHERE s.startTime > :currentTime",
            countQuery = "SELECT COUNT(DISTINCT s.movie.id) FROM Showtime s WHERE s.startTime > :currentTime"
    )
    Page<Movie> findMoviesWithFutureShowtimes(@Param("currentTime") LocalDateTime currentTime, Pageable pageable);
}
