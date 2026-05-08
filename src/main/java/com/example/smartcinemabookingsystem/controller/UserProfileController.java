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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;

    @GetMapping
    public String viewProfile(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", loggedInUser);
        return "profile/view";
    }

    @GetMapping("/edit")
    public String editProfileForm(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", loggedInUser);
        return "profile/edit";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute User user, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Update only allowed fields
        loggedInUser.setFullName(user.getFullName());
        loggedInUser.setEmail(user.getEmail());
        loggedInUser.setPhone(user.getPhone());
        // Password and username should have separate update forms for security

        try {
            userService.updateUserProfile(loggedInUser);
            session.setAttribute("loggedInUser", loggedInUser); // Update session with new data
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật hồ sơ: " + e.getMessage());
        }
        return "redirect:/profile";
    }
}
