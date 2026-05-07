package com.example.smartcinemabookingsystem.controller;

import com.example.smartcinemabookingsystem.model.Booking;
import com.example.smartcinemabookingsystem.model.Seat;
import com.example.smartcinemabookingsystem.model.Showtime;
import com.example.smartcinemabookingsystem.model.User;
import com.example.smartcinemabookingsystem.repository.ShowtimeRepository;
import com.example.smartcinemabookingsystem.repository.TicketRepository;
import com.example.smartcinemabookingsystem.service.BookingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final ShowtimeRepository showtimeRepository;
    private final TicketRepository ticketRepository;

    @GetMapping("/{showtimeId}")
    public String selectSeats(@PathVariable Long showtimeId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }

        Showtime showtime = showtimeRepository.findById(showtimeId).orElse(null);
        if (showtime == null) return "redirect:/";

        List<Seat> seats = bookingService.getAvailableSeats(showtimeId);
        List<Long> bookedSeatIds = ticketRepository.findByShowtimeId(showtimeId)
                .stream().map(t -> t.getSeat().getId()).collect(Collectors.toList());

        model.addAttribute("showtime", showtime);
        model.addAttribute("seats", seats);
        model.addAttribute("bookedSeatIds", bookedSeatIds);
        
        return "booking/seat-selection";
    }

    @PostMapping("/confirm")
    public String confirmBooking(@RequestParam Long showtimeId, 
                                 @RequestParam String seatIds, 
                                 HttpSession session, 
                                 Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        try {
            List<Long> seatIdList = Arrays.stream(seatIds.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            
            Booking booking = bookingService.createBooking(user, showtimeId, seatIdList);
            model.addAttribute("booking", booking);
            return "booking/success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/booking/" + showtimeId + "?error";
        }
    }

    @GetMapping("/history")
    public String bookingHistory(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        model.addAttribute("bookings", bookingService.getBookingsByUser(user.getId()));
        return "booking/history";
    }
}
