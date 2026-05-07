package com.example.smartcinemabookingsystem.repository;

import com.example.smartcinemabookingsystem.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByShowtimeId(Long showtimeId);
    boolean existsByShowtimeIdAndSeatId(Long showtimeId, Long seatId);
}
