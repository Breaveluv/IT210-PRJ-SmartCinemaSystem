package com.example.smartcinemabookingsystem.controller;

import com.example.smartcinemabookingsystem.model.Movie;
import com.example.smartcinemabookingsystem.service.MovieService;
import com.example.smartcinemabookingsystem.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    private final ShowtimeService showtimeService;

    @GetMapping
    public String listMovies(Model model) {
        model.addAttribute("movies", movieService.getAllMovies());
        return "movies/list";
    }

    @GetMapping("/{id}")
    public String movieDetails(@PathVariable Long id, Model model) {
        Movie movie = movieService.getMovieById(id);
        if (movie == null) {
            return "redirect:/movies"; // Redirect if movie not found
        }
        model.addAttribute("movie", movie);
        model.addAttribute("showtimes", showtimeService.getShowtimesByMovieId(id));
        return "movies/details";
    }
}
