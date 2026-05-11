package com.example.smartcinemabookingsystem.service;

import com.example.smartcinemabookingsystem.dto.BookingHistoryDTO; // Import DTO
import com.example.smartcinemabookingsystem.exception.BookingConflictException;
import com.example.smartcinemabookingsystem.model.*;
import com.example.smartcinemabookingsystem.repository.BookingRepository;
import com.example.smartcinemabookingsystem.repository.SeatRepository;
import com.example.smartcinemabookingsystem.repository.ShowtimeRepository;
import com.example.smartcinemabookingsystem.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal; // Import BigDecimal
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Import Collectors

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final RoomService roomService;

    // Thời gian tối thiểu trước suất chiếu để cho phép hủy vé (ví dụ: 24 giờ)
    private static final int MIN_HOURS_TO_CANCEL = 24;

    /**
     * Lấy tất cả danh sách đặt vé (Dùng cho AdminController)
     * Giải quyết lỗi: cannot find symbol: method getAllBookings()
     */
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    /**
     * Lấy chi tiết một đơn đặt vé theo ID (Dùng cho AdminController)
     */
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt vé với ID: " + id));
    }

    public List<Seat> getAvailableSeats(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId).orElseThrow();
        roomService.ensureSeatCount(showtime.getRoom());
        List<Seat> roomSeats = seatRepository.findByRoomIdOrderByIdAsc(showtime.getRoom().getId());
        Integer customTotalSeats = showtime.getCustomTotalSeats();

        if (customTotalSeats != null && customTotalSeats > 0 && customTotalSeats < roomSeats.size()) {
            return roomSeats.subList(0, customTotalSeats);
        }

        return roomSeats;
    }

    @Transactional
    public Booking createBooking(User user, Long showtimeId, List<Long> seatIds) {
        Showtime showtime = showtimeRepository.findById(showtimeId).orElseThrow();
        if (!showtime.getStartTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Suat chieu da qua gio, khong the dat ve.");
        }
        long bookedSeats = ticketRepository.countByShowtimeIdAndBookingStatusNot(showtimeId, Booking.BookingStatus.CANCELLED);
        long effectiveTotalSeats = showtime.getCustomTotalSeats() != null && showtime.getCustomTotalSeats() > 0
                ? showtime.getCustomTotalSeats()
                : showtime.getRoom().getTotalSeats();
        if (bookedSeats >= effectiveTotalSeats) {
            throw new IllegalStateException("Suat chieu da het ve.");
        }
        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("Vui long chon it nhat mot ghe.");
        }

        List<Long> validSeatIds = getAvailableSeats(showtimeId).stream()
                .map(Seat::getId)
                .toList();

        for (Long seatId : seatIds) {
            if (!validSeatIds.contains(seatId)) {
                throw new IllegalArgumentException("Ghe da chon khong nam trong so luong ghe cua suat chieu nay.");
            }
            // Check if the seat is already booked for this showtime and not cancelled
            if (ticketRepository.existsByShowtimeIdAndSeatIdAndBookingStatusNot(showtimeId, seatId, Booking.BookingStatus.CANCELLED)) {
                Seat seat = seatRepository.findById(seatId).orElseThrow(() -> new IllegalArgumentException("Ghế không tồn tại"));
                throw new BookingConflictException("Ghế " + seat.getSeatName() + " đã được đặt bởi người khác! Vui lòng chọn lại.");
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

    // Method for booking history with full details, mapped to DTO
    public List<BookingHistoryDTO> getDetailedBookingHistoryForUser(Long userId) {
        List<Booking> bookings = bookingRepository.findBookingHistoryByUserId(userId);

        return bookings.stream().map(booking -> {
            BookingHistoryDTO dto = new BookingHistoryDTO();
            dto.setBookingId(booking.getId());
            dto.setBookingTime(booking.getBookingTime());
            dto.setTotalAmount(BigDecimal.valueOf(booking.getTotalAmount())); // Sửa lỗi tại đây
            dto.setBookingStatus(booking.getStatus().name());

            // Assuming all tickets in a booking belong to the same showtime
            if (!booking.getTickets().isEmpty()) {
                Ticket firstTicket = booking.getTickets().get(0);
                Showtime showtime = firstTicket.getShowtime();
                Movie movie = showtime.getMovie();
                Room room = showtime.getRoom();

                dto.setShowtimeId(showtime.getId());
                dto.setShowtimeStartTime(showtime.getStartTime());
                dto.setShowtimePrice(BigDecimal.valueOf(showtime.getPrice()));
                dto.setMovieId(movie.getId());
                dto.setMovieTitle(movie.getTitle());
                dto.setMoviePosterUrl(movie.getPosterUrl());

                dto.setRoomId(room.getId());
                dto.setRoomName(room.getName());

                // Collect all seat names for this booking
                List<String> seatNames = booking.getTickets().stream()
                        .map(ticket -> ticket.getSeat().getSeatName())
                        .collect(Collectors.toList());
                dto.setSeatNames(seatNames);
            }
            return dto;
        }).collect(Collectors.toList());
    }


    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy đơn đặt vé với ID: " + bookingId);
        }

        Booking booking = bookingOpt.get();

        // Check if the booking belongs to the user
        if (!booking.getUser().getId().equals(userId)) {
            throw new SecurityException("Bạn không có quyền hủy đơn đặt vé này.");
        }

        // Check if the booking is already cancelled
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new IllegalStateException("Đơn đặt vé này đã bị hủy trước đó.");
        }

        // Get the showtime from the first ticket
        if (booking.getTickets().isEmpty()) {
            throw new IllegalStateException("Đơn đặt vé không có vé nào.");
        }
        Showtime showtime = booking.getTickets().get(0).getShowtime();

        // Check cancellation time condition
        if (LocalDateTime.now().plusHours(MIN_HOURS_TO_CANCEL).isAfter(showtime.getStartTime())) {
            throw new IllegalStateException("Không thể hủy vé vì suất chiếu sẽ bắt đầu trong vòng " + MIN_HOURS_TO_CANCEL + " giờ tới.");
        }

        // Update booking status to CANCELLED
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }
}
