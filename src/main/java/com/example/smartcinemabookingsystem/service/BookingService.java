package com.example.smartcinemabookingsystem.service;

import com.example.smartcinemabookingsystem.model.*;
import com.example.smartcinemabookingsystem.repository.BookingRepository;
import com.example.smartcinemabookingsystem.repository.SeatRepository;
import com.example.smartcinemabookingsystem.repository.ShowtimeRepository;
import com.example.smartcinemabookingsystem.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;

    public List<Seat> getAvailableSeats(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId).orElseThrow();
        return seatRepository.findByRoomId(showtime.getRoom().getId());
    }

    @Transactional
    public Booking createBooking(User user, Long showtimeId, List<Long> seatIds) {
        Showtime showtime = showtimeRepository.findById(showtimeId).orElseThrow();
        
        for (Long seatId : seatIds) {
            if (ticketRepository.existsByShowtimeIdAndSeatId(showtimeId, seatId)) {
                throw new RuntimeException("Một trong các ghế đã được đặt bởi người khác!");
            }
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setTotalAmount(showtime.getPrice() * seatIds.size());
        
        Booking savedBooking = bookingRepository.save(booking);

        List<Ticket> tickets = new ArrayList<>();
        for (Long seatId : seatIds) {
            Seat seat = seatRepository.findById(seatId).orElseThrow();
            Ticket ticket = new Ticket();
            ticket.setBooking(savedBooking);
            ticket.setShowtime(showtime);
            ticket.setSeat(seat);
            ticket.setPrice(showtime.getPrice());
            tickets.add(ticketRepository.save(ticket));
        }
        
        savedBooking.setTickets(tickets);
        return savedBooking;
    }

    public List<Booking> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserId(userId);
    }
}
