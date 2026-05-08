package com.example.smartcinemabookingsystem.controller;

import com.example.smartcinemabookingsystem.model.User;
import com.example.smartcinemabookingsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users/list";
    }

    @GetMapping("/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", User.Role.values()); // Pass all available roles to the form
        return "admin/users/form";
    }

    @PostMapping("/save")
    public String saveUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            // In a real app, you'd handle password hashing here if it's a new user or password change
            // For now, we're saving it as is (not recommended for production)
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Người dùng đã được lưu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu người dùng: " + e.getMessage());
            if (user.getId() == null) {
                return "redirect:/admin/users/new";
            } else {
                return "redirect:/admin/users/edit/" + user.getId();
            }
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return userService.getUserById(id).map(user -> {
            model.addAttribute("user", user);
            model.addAttribute("roles", User.Role.values());
            return "admin/users/form";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng.");
            return "redirect:/admin/users";
        });
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Người dùng đã được xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa người dùng: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
