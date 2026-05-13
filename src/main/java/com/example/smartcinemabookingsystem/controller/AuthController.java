package com.example.smartcinemabookingsystem.controller;

import com.example.smartcinemabookingsystem.model.User;
import com.example.smartcinemabookingsystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
        model.addAttribute("username", username);
        model.addAttribute("password", password);

        // Validate input không rỗng
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("error", "Tên đăng nhập không được để trống");
            return "login";
        }
        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "Mật khẩu không được để trống");
            return "login";
        }

        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            User loggedInUser = userOpt.get();
            session.setAttribute("loggedInUser", loggedInUser);
            
            if (loggedInUser.getRole() == User.Role.ADMIN) {
                return "redirect:/admin/dashboard"; // Redirect Admin to admin dashboard
            } else {
                return "redirect:/"; // Redirect other users to home page
            }
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
    public String processRegister(@Valid @ModelAttribute User user,
                                  BindingResult bindingResult,
                                  Model model) {
        // Kiểm tra validation errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            return "register";
        }

        // Kiểm tra username đã tồn tại
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Tên đăng nhập đã được sử dụng");
            model.addAttribute("user", user);
            return "register";
        }

        // Kiểm tra email đã tồn tại
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("error", "Email đã được sử dụng");
            model.addAttribute("user", user);
            return "register";
        }

        try {
            userService.registerNewUser(user);
            return "redirect:/login?success";
        } catch (Exception e) {
            model.addAttribute("error", "Đã có lỗi xảy ra. Vui lòng thử lại.");
            model.addAttribute("user", user);
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}
