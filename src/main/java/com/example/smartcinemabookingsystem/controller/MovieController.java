package com.example.smartcinemabookingsystem.controller;

import com.example.smartcinemabookingsystem.model.Movie;
import com.example.smartcinemabookingsystem.service.MovieService;
import com.example.smartcinemabookingsystem.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/movie") // Đã thay đổi từ "/movies" thành "/movie"
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    private final ShowtimeService showtimeService;

    @GetMapping
    public String listMovies(@RequestParam(defaultValue = "0") int page, 
                             @RequestParam(defaultValue = "8") int size, 
                             Model model) {
        Page<Movie> moviePage = movieService.getPaginatedMovies(page, size);
        model.addAttribute("movies", moviePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", moviePage.getTotalPages());
        model.addAttribute("totalItems", moviePage.getTotalElements());
        return "movies/list";
    }

    @GetMapping("/{id}")
    public String movieDetails(@PathVariable Long id, Model model) {
        Movie movie = movieService.getMovieById(id).orElse(null);
        if (movie == null) {
            return "redirect:/movie";
        }
        model.addAttribute("movie", movie);
        model.addAttribute("showtimes", showtimeService.getShowtimesByMovieId(id));
        return "movies/details";
    }
}
