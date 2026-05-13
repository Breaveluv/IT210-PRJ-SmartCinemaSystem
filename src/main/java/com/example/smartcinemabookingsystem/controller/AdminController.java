package com.example.smartcinemabookingsystem.controller;

import com.example.smartcinemabookingsystem.model.Booking;
import com.example.smartcinemabookingsystem.service.BookingService;
import com.example.smartcinemabookingsystem.service.MovieService;
import com.example.smartcinemabookingsystem.service.ShowtimeService;
import com.example.smartcinemabookingsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MovieService movieService;
    private final ShowtimeService showtimeService;
    private final UserService userService;
    private final BookingService bookingService;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        var allBookings = bookingService.getAllBookings();
        var allMovies = movieService.getAllMovies();

        model.addAttribute("totalMovies", allMovies.size());
        model.addAttribute("totalShowtimes", showtimeService.getAllShowtimes().size());
        model.addAttribute("totalUsers", userService.getAllUsers().size());
        model.addAttribute("totalBookings", allBookings.size());

        // Tính tổng doanh thu
        double totalRevenue = allBookings.stream()
                .mapToDouble(booking -> booking.getTotalAmount() != 0 ? booking.getTotalAmount() : 0)
                .sum();
        model.addAttribute("totalRevenue", totalRevenue);

        // Lấy 5 đơn đặt vé gần đây nhất
        var recentBookings = allBookings.stream()
                .sorted((a, b) -> b.getBookingTime().compareTo(a.getBookingTime()))
                .limit(5)
                .toList();
        model.addAttribute("recentBookings", recentBookings);

        model.addAttribute("movies", allMovies);
        return "admin/dashboard";
    }

    // CORE-06: Bookings Management
    @GetMapping("/bookings")
    public String bookingsList(Model model) {
        model.addAttribute("bookings", bookingService.getAllBookings());
        return "admin/bookings/list";
    }

    @GetMapping("/bookings/{id}")
    public String bookingDetail(@PathVariable Long id, Model model) {
        var booking = bookingService.getBookingById(id);
        model.addAttribute("booking", booking);
        return "admin/bookings/detail";
    }

    @PostMapping("/bookings/{id}/confirm")
    public String confirmBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bookingService.updateBookingStatus(id, Booking.BookingStatus.CONFIRMED);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận đơn đặt vé.");
        return "redirect:/admin/bookings/" + id;
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bookingService.updateBookingStatus(id, Booking.BookingStatus.CANCELLED);
        redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn đặt vé.");
        return "redirect:/admin/bookings/" + id;
    }

}
