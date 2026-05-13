package com.example.smartcinemabookingsystem.controller;

import com.example.smartcinemabookingsystem.model.Movie;
import com.example.smartcinemabookingsystem.service.GenreService;
import com.example.smartcinemabookingsystem.service.MovieService;
import jakarta.validation.Valid; // Import @Valid
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // Import BindingResult
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors; // Import Collectors

@Controller
@RequestMapping("/admin/movies")
@RequiredArgsConstructor
public class AdminMovieController {

    private final MovieService movieService;
    private final GenreService genreService;

    @GetMapping
    public String listMovies(Model model) {
        model.addAttribute("movies", movieService.getAllMovies());
        return "admin/movies/list";
    }

    @GetMapping("/new")
    public String newMovieForm(Model model) {
        model.addAttribute("movie", new Movie());
        model.addAttribute("allGenres", genreService.getAllGenres());
        return "admin/movies/form";
    }

    @PostMapping("/save")
    public String saveMovie(@Valid @ModelAttribute Movie movie,
                            BindingResult bindingResult,
                            @RequestParam(value = "genreIds", required = false) List<Long> genreIds,
                            Model model,
                            RedirectAttributes redirectAttributes) {

        // Sync genres from genreIds to the movie object to preserve selection on error
        if (genreIds != null && !genreIds.isEmpty()) {
            java.util.Set<com.example.smartcinemabookingsystem.model.Genre> genres = genreIds.stream()
                    .map(id -> genreService.getGenreById(id).orElse(null))
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toSet());
            movie.setGenres(genres);
        } else {
            movie.setGenres(new java.util.HashSet<>());
            bindingResult.rejectValue("genres", "error.movie", "Phim phải có ít nhất một thể loại");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("allGenres", genreService.getAllGenres());
            return "admin/movies/form";
        }

        try {
            movieService.saveMovie(movie);
            redirectAttributes.addFlashAttribute("successMessage", "Phim đã được lưu thành công!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            model.addAttribute("allGenres", genreService.getAllGenres());
            return "admin/movies/form";
        }
        return "redirect:/admin/movies";
    }

    @GetMapping("/edit/{id}")
    public String editMovieForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return movieService.getMovieById(id).map(movie -> {
            model.addAttribute("movie", movie);
            model.addAttribute("allGenres", genreService.getAllGenres());
            return "admin/movies/form";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy phim.");
            return "redirect:/admin/movies";
        });
    }

    @GetMapping("/delete/{id}")
    public String deleteMovie(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            movieService.deleteMovie(id);
            redirectAttributes.addFlashAttribute("successMessage", "Phim đã được xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa phim: " + e.getMessage());
        }
        return "redirect:/admin/movies";
    }
}
