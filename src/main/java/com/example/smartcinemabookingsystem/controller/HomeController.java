package com.example.smartcinemabookingsystem.controller;

import com.example.smartcinemabookingsystem.model.Movie;
import com.example.smartcinemabookingsystem.model.Showtime;
import com.example.smartcinemabookingsystem.service.MovieService;
import com.example.smartcinemabookingsystem.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MovieService movieService;
    private final ShowtimeService showtimeService;

    @GetMapping("/")
    public String home(@RequestParam(defaultValue = "0") int page, 
                       @RequestParam(defaultValue = "8") int size, 
                       Model model) {
        Page<Movie> moviePage = movieService.getPaginatedMovies(page, size);
        model.addAttribute("movies", moviePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", moviePage.getTotalPages());
        model.addAttribute("totalItems", moviePage.getTotalElements());

        // CORE-08: Add upcoming showtimes to the model
        List<Showtime> upcomingShowtimes = showtimeService.getUpcomingShowtimesWithAvailability();
        model.addAttribute("upcomingShowtimes", upcomingShowtimes);

        // FIX: Expose showtimeService to the Thymeleaf template
        model.addAttribute("showtimeService", showtimeService);

        return "home";
    }
}
