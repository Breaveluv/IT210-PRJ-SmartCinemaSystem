package com.example.smartcinemabookingsystem.controller;

import com.example.smartcinemabookingsystem.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MovieService movieService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("movies", movieService.getAllMovies());
        return "home";
    }
}
