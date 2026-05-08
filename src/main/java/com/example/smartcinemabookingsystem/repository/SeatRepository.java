package com.example.smartcinemabookingsystem.repository;

import com.example.smartcinemabookingsystem.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByRoomId(Long roomId);

    // Added for CORE-08 to count total seats in a room
    long countByRoomId(Long roomId);
}
