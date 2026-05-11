package com.example.smartcinemabookingsystem.service;

import com.example.smartcinemabookingsystem.exception.ShowtimeConflictException;
import com.example.smartcinemabookingsystem.model.Booking;
import com.example.smartcinemabookingsystem.model.Showtime;
import com.example.smartcinemabookingsystem.repository.ShowtimeRepository;
import com.example.smartcinemabookingsystem.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final TicketRepository ticketRepository;
    private final RoomService roomService;

    private static final int CLEANUP_BUFFER_MINUTES = 15;

    public List<Showtime> getShowtimesByMovieId(Long movieId) {
        return showtimeRepository.findByMovieId(movieId).stream()
                .map(this::decorateAvailability)
                .collect(Collectors.toList());
    }

    public List<Showtime> getFutureShowtimesByMovieId(Long movieId) {
        return showtimeRepository.findByMovieId(movieId).stream()
                .filter(showtime -> showtime.getStartTime().isAfter(LocalDateTime.now()))
                .map(this::decorateAvailability)
                .collect(Collectors.toList());
    }

    public List<Showtime> getAllShowtimes() {
        return showtimeRepository.findAll().stream()
                .map(this::decorateAvailability)
                .collect(Collectors.toList());
    }

    public Optional<Showtime> getShowtimeById(Long id) {
        return showtimeRepository.findById(id).map(this::decorateAvailability);
    }

    @Transactional
    public Showtime saveShowtime(Showtime newShowtime) {
        if (newShowtime.getMovie() == null || newShowtime.getMovie().getId() == null) {
            throw new IllegalArgumentException("Movie information is required for showtime.");
        }
        if (newShowtime.getRoom() == null || newShowtime.getRoom().getId() == null) {
            throw new IllegalArgumentException("Room information is required for showtime.");
        }
        if (newShowtime.getStartTime() == null) {
            throw new IllegalArgumentException("Start time is required for showtime.");
        }

        LocalDateTime movieEndTime = newShowtime.getStartTime().plusMinutes(newShowtime.getMovie().getDuration());
        newShowtime.setEndTime(movieEndTime);

        roomService.ensureSeatCount(newShowtime.getRoom());
        Integer customTotalSeats = newShowtime.getCustomTotalSeats();
        if (customTotalSeats != null && customTotalSeats > newShowtime.getRoom().getTotalSeats()) {
            throw new IllegalArgumentException("So ghe cua suat chieu khong duoc lon hon tong so ghe cua phong.");
        }

        LocalDateTime showtimeEndTimeWithBuffer = movieEndTime.plusMinutes(CLEANUP_BUFFER_MINUTES);
        List<Showtime> existingShowtimesInRoom = showtimeRepository.findByRoomId(newShowtime.getRoom().getId());

        for (Showtime existingShowtime : existingShowtimesInRoom) {
            if (newShowtime.getId() != null && newShowtime.getId().equals(existingShowtime.getId())) {
                continue;
            }

            LocalDateTime existingShowtimeEndTimeWithBuffer =
                    existingShowtime.getEndTime().plusMinutes(CLEANUP_BUFFER_MINUTES);
            boolean overlap = newShowtime.getStartTime().isBefore(existingShowtimeEndTimeWithBuffer)
                    && existingShowtime.getStartTime().isBefore(showtimeEndTimeWithBuffer);

            if (overlap) {
                throw new ShowtimeConflictException(
                        "Phong " + newShowtime.getRoom().getName()
                                + " da co suat chieu khac tu "
                                + existingShowtime.getStartTime().toLocalTime()
                                + " den " + existingShowtimeEndTimeWithBuffer.toLocalTime()
                                + " (bao gom thoi gian don phong). Vui long chon thoi gian khac."
                );
            }
        }

        return showtimeRepository.save(newShowtime);
    }

    public void deleteShowtime(Long id) {
        showtimeRepository.deleteById(id);
    }

    public List<Showtime> getUpcomingShowtimesWithAvailability() {
        return showtimeRepository.findByStartTimeAfter(LocalDateTime.now()).stream()
                .map(this::decorateAvailability)
                .collect(Collectors.toList());
    }

    public boolean isShowtimeSoldOut(Long showtimeId) {
        Optional<Showtime> showtimeOpt = showtimeRepository.findById(showtimeId);
        if (showtimeOpt.isEmpty()) {
            return false;
        }

        Showtime showtime = showtimeOpt.get();
        long bookedSeats = ticketRepository.countByShowtimeIdAndBookingStatusNot(
                showtime.getId(), Booking.BookingStatus.CANCELLED);
        return bookedSeats >= getEffectiveTotalSeats(showtime);
    }

    public Showtime decorateAvailability(Showtime showtime) {
        boolean started = !showtime.getStartTime().isAfter(LocalDateTime.now());
        boolean soldOut = isShowtimeSoldOut(showtime.getId());

        showtime.setStarted(started);
        showtime.setSoldOut(soldOut);
        if (started) {
            showtime.setStatusLabel("Da qua gio");
        } else if (soldOut) {
            showtime.setStatusLabel("Het ve");
        } else {
            showtime.setStatusLabel("Con ve");
        }
        return showtime;
    }

    private long getEffectiveTotalSeats(Showtime showtime) {
        if (showtime.getCustomTotalSeats() != null && showtime.getCustomTotalSeats() > 0) {
            return showtime.getCustomTotalSeats();
        }
        return showtime.getRoom().getTotalSeats();
    }
}
