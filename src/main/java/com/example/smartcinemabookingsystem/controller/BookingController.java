package com.example.smartcinemabookingsystem.controller;

import com.example.smartcinemabookingsystem.exception.BookingConflictException;
import com.example.smartcinemabookingsystem.model.Booking;
import com.example.smartcinemabookingsystem.model.Seat;
import com.example.smartcinemabookingsystem.model.Showtime;
import com.example.smartcinemabookingsystem.model.User;
import com.example.smartcinemabookingsystem.repository.ShowtimeRepository;
import com.example.smartcinemabookingsystem.repository.TicketRepository;
import com.example.smartcinemabookingsystem.service.BookingService;
import com.example.smartcinemabookingsystem.service.ShowtimeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final ShowtimeService showtimeService;
    private final ShowtimeRepository showtimeRepository;
    private final TicketRepository ticketRepository;

    @GetMapping("/{showtimeId}")
    public String selectSeats(@PathVariable Long showtimeId, Model model, HttpSession session,
                              @RequestParam(value = "error", required = false) String error) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }

        Showtime showtime = showtimeService.getShowtimeById(showtimeId).orElse(null);
        if (showtime == null) return "redirect:/";
        if (showtime.isStarted()) {
            return "redirect:/movie/" + showtime.getMovie().getId();
        }
        if (showtime.isSoldOut()) {
            return "redirect:/movie/" + showtime.getMovie().getId();
        }

        List<Seat> seats = bookingService.getAvailableSeats(showtimeId);

        List<Long> bookedSeatIds = ticketRepository.findByShowtimeId(showtimeId)
                .stream()
                .filter(ticket -> ticket.getBooking().getStatus() != Booking.BookingStatus.CANCELLED)
                .map(t -> t.getSeat().getId())
                .collect(Collectors.toList());


        model.addAttribute("showtime", showtime);
        model.addAttribute("seats", seats);
        model.addAttribute("bookedSeatIds", bookedSeatIds);
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }
        
        return "booking/seat-selection";
    }

    @PostMapping("/confirm")
    public String confirmBooking(@RequestParam Long showtimeId, 
                                 @RequestParam(value = "seatIds", required = false) List<Long> seatIds, 
                                 HttpSession session, 
                                 RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        try {
            Booking booking = bookingService.createBooking(user, showtimeId, seatIds);
            redirectAttributes.addFlashAttribute("booking", booking);
            return "redirect:/booking/success";
        } catch (BookingConflictException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/booking/" + showtimeId;
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Đã xảy ra lỗi khi đặt vé: " + e.getMessage());
            return "redirect:/booking/" + showtimeId;
        }
    }

    @GetMapping("/success")
    public String bookingSuccess(Model model) {
        if (!model.containsAttribute("booking")) {
            return "redirect:/";
        }
        return "booking/success";
    }

    @GetMapping("/history")
    public String bookingHistory(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        model.addAttribute("bookings", bookingService.getDetailedBookingHistoryForUser(user.getId()));
        return "booking/history";
    }

    @GetMapping("/cancel/{bookingId}")
    public String cancelBooking(@PathVariable Long bookingId, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            bookingService.cancelBooking(bookingId, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Hủy vé thành công!");
        } catch (SecurityException | IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi hủy vé: " + e.getMessage());
        }
        return "redirect:/booking/history";
    }
}
