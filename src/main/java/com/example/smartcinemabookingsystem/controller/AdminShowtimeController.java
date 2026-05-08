package com.example.smartcinemabookingsystem.controller;

import com.example.smartcinemabookingsystem.model.Showtime;
import com.example.smartcinemabookingsystem.service.MovieService;
import com.example.smartcinemabookingsystem.service.RoomService;
import com.example.smartcinemabookingsystem.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/showtimes")
@RequiredArgsConstructor
public class AdminShowtimeController {

    private final ShowtimeService showtimeService;
    private final MovieService movieService;
    private final RoomService roomService;

    @GetMapping
    public String listShowtimes(Model model) {
        model.addAttribute("showtimes", showtimeService.getAllShowtimes());
        return "admin/showtimes/list";
    }

    @GetMapping("/new")
    public String newShowtimeForm(Model model) {
        model.addAttribute("showtime", new Showtime());
        model.addAttribute("movies", movieService.getAllMovies()); // Need to get all movies, not paginated
        model.addAttribute("rooms", roomService.getAllRooms());
        return "admin/showtimes/form";
    }

    @PostMapping("/save")
    public String saveShowtime(@ModelAttribute Showtime showtime, RedirectAttributes redirectAttributes) {
        try {
            // Fetch Movie and Room objects as only IDs are passed from form
            showtime.setMovie(movieService.getMovieById(showtime.getMovie().getId()));
            showtime.setRoom(roomService.getRoomById(showtime.getRoom().getId()).orElse(null));

            showtimeService.saveShowtime(showtime);
            redirectAttributes.addFlashAttribute("successMessage", "Suất chiếu đã được lưu thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            // If it's a new showtime, redirect to new form, else to edit form
            if (showtime.getId() == null) {
                return "redirect:/admin/showtimes/new";
            } else {
                return "redirect:/admin/showtimes/edit/" + showtime.getId();
            }
        }
        return "redirect:/admin/showtimes";
    }

    @GetMapping("/edit/{id}")
    public String editShowtimeForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return showtimeService.getShowtimeById(id).map(showtime -> {
            model.addAttribute("showtime", showtime);
            model.addAttribute("movies", movieService.getAllMovies()); // Need to get all movies, not paginated
            model.addAttribute("rooms", roomService.getAllRooms());
            return "admin/showtimes/form";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy suất chiếu.");
            return "redirect:/admin/showtimes";
        });
    }

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
}
