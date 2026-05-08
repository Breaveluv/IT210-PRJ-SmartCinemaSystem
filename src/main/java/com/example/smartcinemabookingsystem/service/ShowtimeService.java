package com.example.smartcinemabookingsystem.service;

import com.example.smartcinemabookingsystem.exception.ShowtimeConflictException;
import com.example.smartcinemabookingsystem.model.Booking;
import com.example.smartcinemabookingsystem.model.Showtime;
import com.example.smartcinemabookingsystem.repository.SeatRepository;
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
    private final SeatRepository seatRepository; // Vẫn giữ để dùng cho các mục đích khác nếu cần
    private final TicketRepository ticketRepository;

    private static final int CLEANUP_BUFFER_MINUTES = 15; // Thời gian dọn phòng

    public List<Showtime> getShowtimesByMovieId(Long movieId) {
        return showtimeRepository.findByMovieId(movieId);
    }

    // Phương thức mới để lấy tất cả suất chiếu (cho Admin)
    public List<Showtime> getAllShowtimes() {
        return showtimeRepository.findAll();
    }

    // Phương thức mới để lấy suất chiếu theo ID
    public Optional<Showtime> getShowtimeById(Long id) {
        return showtimeRepository.findById(id);
    }

    @Transactional
    public Showtime saveShowtime(Showtime newShowtime) {
        // 1. Fetch movie to get its duration if not already set (e.g., in case of update)
        if (newShowtime.getMovie() == null || newShowtime.getMovie().getId() == null) {
            throw new IllegalArgumentException("Movie information is required for showtime.");
        }
        // In a real application, you'd fetch the full Movie object here if needed,
        // but for conflict check, we only need duration which should be available
        // if the movie object is properly managed by JPA or fetched.
        // For simplicity, assuming newShowtime.getMovie().getDuration() is already populated
        // or the movie object is managed. If not, you'd inject MovieService and fetch it.

        // 2. Calculate actual end time including movie duration
        LocalDateTime movieEndTime = newShowtime.getStartTime().plusMinutes(newShowtime.getMovie().getDuration());
        newShowtime.setEndTime(movieEndTime);

        // 3. Calculate end time with cleanup buffer
        LocalDateTime showtimeEndTimeWithBuffer = movieEndTime.plusMinutes(CLEANUP_BUFFER_MINUTES);

        // 4. Check for conflicts in the same room
        List<Showtime> existingShowtimesInRoom = showtimeRepository.findByRoomId(newShowtime.getRoom().getId());

        for (Showtime existingShowtime : existingShowtimesInRoom) {
            // If it's an update, exclude the showtime itself from conflict check
            if (newShowtime.getId() != null && newShowtime.getId().equals(existingShowtime.getId())) {
                continue;
            }

            LocalDateTime existingShowtimeEndTimeWithBuffer = existingShowtime.getEndTime().plusMinutes(CLEANUP_BUFFER_MINUTES);

            // Check for overlap: [start1, end1_buffer] overlaps with [start2, end2_buffer] if (start1 < end2_buffer AND start2 < end1_buffer)
            boolean overlap = (newShowtime.getStartTime().isBefore(existingShowtimeEndTimeWithBuffer) &&
                               existingShowtime.getStartTime().isBefore(showtimeEndTimeWithBuffer));

            if (overlap) {
                throw new ShowtimeConflictException(
                        "Phòng " + newShowtime.getRoom().getName() +
                        " đã có suất chiếu khác từ " +
                        existingShowtime.getStartTime().toLocalTime() + " đến " +
                        existingShowtimeEndTimeWithBuffer.toLocalTime() +
                        " (bao gồm thời gian dọn phòng). Vui lòng chọn thời gian khác."
                );
            }
        }

        return showtimeRepository.save(newShowtime);
    }

    // Phương thức mới để xóa suất chiếu
    public void deleteShowtime(Long id) {
        showtimeRepository.deleteById(id);
    }

    /**
     * CORE-08: Lấy danh sách các suất chiếu sắp tới và kiểm tra trạng thái "Hết vé".
     * Suất chiếu phải tự động thay đổi trạng thái hoặc bị ẩn đi khi:
     * - Thời gian hiện tại đã vượt quá thời gian bắt đầu của suất chiếu (Ẩn hoàn toàn).
     * - Toàn bộ ghế trong phòng của suất chiếu đó đã được đặt (Sold out) (Vẫn cho xem sơ đồ ghế nhưng hiển thị nhãn “Hết vé”).
     */
    public List<Showtime> getUpcomingShowtimesWithAvailability() {
        // Filter out showtimes that have already started
        List<Showtime> upcomingShowtimes = showtimeRepository.findByStartTimeAfter(LocalDateTime.now());

        // For each upcoming showtime, determine if it's sold out
        return upcomingShowtimes.stream().map(showtime -> {
            // Sử dụng logic effectiveTotalSeats tương tự như isShowtimeSoldOut
            long effectiveTotalSeats;
            if (showtime.getCustomTotalSeats() != null && showtime.getCustomTotalSeats() > 0) {
                effectiveTotalSeats = showtime.getCustomTotalSeats();
            } else {
                // Lấy tổng số ghế từ thuộc tính totalSeats của Room
                effectiveTotalSeats = showtime.getRoom().getTotalSeats();
            }

            long bookedSeats = ticketRepository.countByShowtimeIdAndBookingStatusNot(showtime.getId(), Booking.BookingStatus.CANCELLED);

            // Nếu cần hiển thị trạng thái sold out trực tiếp trên đối tượng Showtime,
            // bạn có thể thêm một trường transient vào Showtime hoặc sử dụng DTO.
            // Hiện tại, logic isShowtimeSoldOut sẽ được gọi riêng trong template.
            return showtime;
        }).collect(Collectors.toList());
    }

    public boolean isShowtimeSoldOut(Long showtimeId) {
        Optional<Showtime> showtimeOpt = showtimeRepository.findById(showtimeId);
        if (showtimeOpt.isEmpty()) {
            return false; // Hoặc ném một ngoại lệ nếu suất chiếu không tồn tại
        }
        Showtime showtime = showtimeOpt.get();

        // Xác định tổng số ghế hiệu dụng cho suất chiếu này
        long effectiveTotalSeats;
        if (showtime.getCustomTotalSeats() != null && showtime.getCustomTotalSeats() > 0) {
            effectiveTotalSeats = showtime.getCustomTotalSeats();
        } else {
            // Lấy tổng số ghế từ thuộc tính totalSeats của Room
            effectiveTotalSeats = showtime.getRoom().getTotalSeats();
        }

        long bookedSeats = ticketRepository.countByShowtimeIdAndBookingStatusNot(showtime.getId(), Booking.BookingStatus.CANCELLED);
        return bookedSeats >= effectiveTotalSeats;
    }
}
