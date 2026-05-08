package com.example.smartcinemabookingsystem.repository;

import com.example.smartcinemabookingsystem.model.Booking;
import com.example.smartcinemabookingsystem.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByShowtimeId(Long showtimeId);
    boolean existsByShowtimeIdAndSeatId(Long showtimeId, Long seatId);
    
    // New method to check for active bookings
    boolean existsByShowtimeIdAndSeatIdAndBookingStatusNot(Long showtimeId, Long seatId, Booking.BookingStatus status);

    // New method for CORE-08: Count active tickets for a showtime
    long countByShowtimeIdAndBookingStatusNot(Long showtimeId, Booking.BookingStatus status);

    // Method to check with pessimistic lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.showtime.id = :showtimeId AND t.seat.id = :seatId AND t.booking.status != :status")
    boolean existsByShowtimeIdAndSeatIdAndBookingStatusNotLocked(@Param("showtimeId") Long showtimeId, @Param("seatId") Long seatId, @Param("status") Booking.BookingStatus status);
}
