package com.example.smartcinemabookingsystem.controller;

import com.example.smartcinemabookingsystem.model.Genre;
import com.example.smartcinemabookingsystem.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/genres")
@RequiredArgsConstructor
public class AdminGenreController {

    private final GenreService genreService;

    @GetMapping
    public String listGenres(Model model) {
        model.addAttribute("genres", genreService.getAllGenres());
        return "admin/genres/list";
    }

    @GetMapping("/new")
    public String newGenreForm(Model model) {
        model.addAttribute("genre", new Genre());
        return "admin/genres/form";
    }

    @PostMapping("/save")
    public String saveGenre(@ModelAttribute Genre genre, RedirectAttributes redirectAttributes) {
        try {
            genreService.saveGenre(genre);
            redirectAttributes.addFlashAttribute("successMessage", "Thể loại đã được lưu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu thể loại: " + e.getMessage());
            if (genre.getId() == null) {
                return "redirect:/admin/genres/new";
            } else {
                return "redirect:/admin/genres/edit/" + genre.getId();
            }
        }
        return "redirect:/admin/genres";
    }

    @GetMapping("/edit/{id}")
    public String editGenreForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return genreService.getGenreById(id).map(genre -> {
            model.addAttribute("genre", genre);
            return "admin/genres/form";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thể loại.");
            return "redirect:/admin/genres";
        });
    }

    @GetMapping("/delete/{id}")
    public String deleteGenre(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            genreService.deleteGenre(id);
            redirectAttributes.addFlashAttribute("successMessage", "Thể loại đã được xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa thể loại: " + e.getMessage());
        }
        return "redirect:/admin/genres";
    }
}
