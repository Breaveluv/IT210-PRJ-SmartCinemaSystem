package com.example.smartcinemabookingsystem.controller;

import com.example.smartcinemabookingsystem.exception.ShowtimeConflictException;
import com.example.smartcinemabookingsystem.model.Movie;
import com.example.smartcinemabookingsystem.model.Showtime;
import com.example.smartcinemabookingsystem.model.Booking;
import com.example.smartcinemabookingsystem.model.Ticket;
import com.example.smartcinemabookingsystem.repository.TicketRepository;
import com.example.smartcinemabookingsystem.service.MovieService;
import com.example.smartcinemabookingsystem.service.RoomService;
import com.example.smartcinemabookingsystem.service.ShowtimeService;
import jakarta.validation.Valid; // Import @Valid
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // Import BindingResult
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/showtimes")
@RequiredArgsConstructor
public class AdminShowtimeController {

    private final ShowtimeService showtimeService;
    private final MovieService movieService;
    private final RoomService roomService;
    private final TicketRepository ticketRepository;

    @GetMapping
    public String listShowtimes(Model model) {
        model.addAttribute("showtimes", showtimeService.getAllShowtimes());
        return "admin/showtimes/list"; // Updated to use admin layout
    }

    @GetMapping("/new")
    public String newShowtimeForm(Model model) {
        model.addAttribute("showtime", new Showtime());
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("rooms", roomService.getAllRooms());
        return "admin/showtimes/form"; // Updated to use admin layout
    }
    // Lưu
    @PostMapping("/save")
    public String saveShowtime(@Valid @ModelAttribute Showtime showtime, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        // If there are validation errors from @Valid
        if (bindingResult.hasErrors()) {
            model.addAttribute("movies", movieService.getAllMovies());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "admin/showtimes/form";
        }

        try {
            // Check if movie and room objects exist (id is nested)
            if (showtime.getMovie() == null || showtime.getMovie().getId() == null) {
                bindingResult.rejectValue("movie.id", "error.showtime", "Vui lòng chọn phim.");
            }
            if (showtime.getRoom() == null || showtime.getRoom().getId() == null) {
                bindingResult.rejectValue("room.id", "error.showtime", "Vui lòng chọn phòng chiếu.");
            }

            if (bindingResult.hasErrors()) {
                model.addAttribute("movies", movieService.getAllMovies());
                model.addAttribute("rooms", roomService.getAllRooms());
                return "admin/showtimes/form";
            }

            // Fetch full objects to ensure they exist and to get duration for endTime calculation
            Movie movie = movieService.getMovieById(showtime.getMovie().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Phim không tồn tại."));
            showtime.setMovie(movie);

            showtime.setRoom(roomService.getRoomById(showtime.getRoom().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Phòng chiếu không tồn tại.")));

            // Calculate endTime based on movie duration
            if (showtime.getStartTime() != null) {
                showtime.setEndTime(showtime.getStartTime().plusMinutes(movie.getDuration()));
            }

            showtimeService.saveShowtime(showtime);
            redirectAttributes.addFlashAttribute("successMessage", "Suất chiếu đã được lưu thành công!");
        } catch (ShowtimeConflictException e) {
            model.addAttribute("errorMessage", "Lỗi xung đột: " + e.getMessage());
            model.addAttribute("movies", movieService.getAllMovies());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "admin/showtimes/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            model.addAttribute("movies", movieService.getAllMovies());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "admin/showtimes/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            model.addAttribute("movies", movieService.getAllMovies());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "admin/showtimes/form";
        }
        return "redirect:/admin/showtimes";
    }
    // Sửa
    @GetMapping("/edit/{id}")
    public String editShowtimeForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return showtimeService.getShowtimeById(id).map(showtime -> {
            model.addAttribute("showtime", showtime);
            model.addAttribute("movies", movieService.getAllMovies());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "admin/showtimes/form";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy suất chiếu.");
            return "redirect:/admin/showtimes";
        });
    }
    // Xóa
    @GetMapping("/delete/{id}")
    public String deleteShowtime(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            showtimeService.deleteShowtime(id);
            redirectAttributes.addFlashAttribute("successMessage", "Suất chiếu đã được xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa suất chiếu: " + e.getMessage());
        }
        return "redirect:/admin/showtimes";
    }

    @GetMapping("/{id:\\d+}")
    public String showtimeDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Showtime showtime = showtimeService.getShowtimeById(id).orElse(null);
        if (showtime == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khong tim thay suat chieu.");
            return "redirect:/admin/showtimes";
        }

        var tickets = ticketRepository.findByShowtimeId(id);
        var bookings = tickets.stream()
                .map(Ticket::getBooking)
                .distinct()
                .toList();
        int totalSeats = showtime.getCustomTotalSeats() != null && showtime.getCustomTotalSeats() > 0
                ? showtime.getCustomTotalSeats()
                : showtime.getRoom().getTotalSeats();
        long bookedSeatCount = tickets.stream()
                .filter(ticket -> ticket.getBooking().getStatus() != Booking.BookingStatus.CANCELLED)
                .count();

        model.addAttribute("showtime", showtime);
        model.addAttribute("tickets", tickets);
        model.addAttribute("bookings", bookings);
        model.addAttribute("totalSeats", totalSeats);
        model.addAttribute("bookedSeatCount", bookedSeatCount);
        model.addAttribute("availableSeatCount", Math.max(0, totalSeats - bookedSeatCount));
        model.addAttribute("occupancyRate", totalSeats == 0 ? 0 : (bookedSeatCount * 100.0) / totalSeats);
        model.addAttribute("revenue", tickets.stream()
                .filter(ticket -> ticket.getBooking().getStatus() != Booking.BookingStatus.CANCELLED)
                .mapToDouble(Ticket::getPrice)
                .sum());
        return "admin/showtimes/detail";
    }
}
