package com.example.smartcinemabookingsystem.controller;

import com.example.smartcinemabookingsystem.model.User;
import com.example.smartcinemabookingsystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username, 
                               @RequestParam String password, 
                               HttpSession session, 
                               Model model) {
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            session.setAttribute("loggedInUser", userOpt.get());
            return "redirect:/";
        }
        model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng");
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute User user) {
        userService.registerNewUser(user);
        return "redirect:/login?success";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}
