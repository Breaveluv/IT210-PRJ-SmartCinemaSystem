package com.example.smartcinemabookingsystem.repository;

import com.example.smartcinemabookingsystem.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);

    // Method to fetch bookings with all related data for history
    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.tickets t " +
           "LEFT JOIN FETCH t.showtime s " +
           "LEFT JOIN FETCH s.movie m " +
           "LEFT JOIN FETCH s.room r " +
           "LEFT JOIN FETCH t.seat se " +
           "WHERE b.user.id = :userId " +
           "ORDER BY b.bookingTime DESC")
    List<Booking> findBookingHistoryByUserId(@Param("userId") Long userId);
}
