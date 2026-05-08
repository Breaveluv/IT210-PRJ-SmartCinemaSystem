package com.example.smartcinemabookingsystem.controller;

import com.example.smartcinemabookingsystem.model.Movie;
import com.example.smartcinemabookingsystem.service.GenreService;
import com.example.smartcinemabookingsystem.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String saveMovie(@ModelAttribute Movie movie, RedirectAttributes redirectAttributes) {
        try {
            movieService.saveMovie(movie);
            redirectAttributes.addFlashAttribute("successMessage", "Phim đã được lưu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu phim: " + e.getMessage());
            if (movie.getId() == null) {
                return "redirect:/admin/movies/new";
            } else {
                return "redirect:/admin/movies/edit/" + movie.getId();
            }
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
