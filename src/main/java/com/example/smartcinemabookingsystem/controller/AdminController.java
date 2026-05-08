package com.example.smartcinemabookingsystem.controller;

import com.example.smartcinemabookingsystem.service.MovieService;
import com.example.smartcinemabookingsystem.service.ShowtimeService;
import com.example.smartcinemabookingsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MovieService movieService;
    private final ShowtimeService showtimeService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("totalMovies", movieService.getAllMovies().size());
        model.addAttribute("totalShowtimes", showtimeService.getAllShowtimes().size());
        model.addAttribute("totalUsers", userService.getAllUsers().size()); // Need to add getAllUsers to UserService
        return "admin/dashboard";
    }
}
