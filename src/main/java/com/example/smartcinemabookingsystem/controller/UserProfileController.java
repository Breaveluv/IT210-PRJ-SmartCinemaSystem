package com.example.smartcinemabookingsystem.controller;

import com.example.smartcinemabookingsystem.model.User;
import com.example.smartcinemabookingsystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import lombok.RequiredArgsConstructor;

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
        // Fetch the latest user data from DB to ensure it's up-to-date
        loggedInUser = userService.getUserById(loggedInUser.getId())
                                  .orElse(loggedInUser); // Fallback to session if not found (shouldn't happen)
        session.setAttribute("loggedInUser", loggedInUser); // Update session with latest data
        model.addAttribute("user", loggedInUser);
        addRoleProfileAttributes(model, loggedInUser);
        return "profile/view";
    }

    @GetMapping("/edit")
    public String editProfileForm(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Fetch the latest user data from DB for editing
        loggedInUser = userService.getUserById(loggedInUser.getId())
                                  .orElse(loggedInUser);
        model.addAttribute("user", loggedInUser);
        addRoleProfileAttributes(model, loggedInUser);
        return "profile/edit";
    }

    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute User user, BindingResult bindingResult, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        // Không cho phép đổi username qua form này
        user.setId(loggedInUser.getId());
        user.setUsername(loggedInUser.getUsername());
        user.setRole(loggedInUser.getRole());
        // Nếu có lỗi validate
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", bindingResult);
            redirectAttributes.addFlashAttribute("user", user);
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin!");
            return "redirect:/profile/edit";
        }
        // Kiểm tra email đã tồn tại (và khác email hiện tại)
        var emailOwner = userService.findByEmail(user.getEmail());
        if (emailOwner.isPresent() && !emailOwner.get().getId().equals(loggedInUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email đã được sử dụng bởi tài khoản khác!");
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/profile/edit";
        }
        try {
            User updatedUser = userService.updateUserProfile(user);
            session.setAttribute("loggedInUser", updatedUser);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật hồ sơ: " + e.getMessage());
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/profile/edit";
        }
        return "redirect:/profile";
    }

    private void addRoleProfileAttributes(Model model, User user) {
        if (user.getRole() == User.Role.ADMIN) {
            model.addAttribute("profileType", "Admin");
            model.addAttribute("profileDescription", "Quan tri he thong, phim, phong, suat chieu va tai khoan.");
            model.addAttribute("primaryActionUrl", "/admin/dashboard");
            model.addAttribute("primaryActionLabel", "Mo trang quan tri");
        } else if (user.getRole() == User.Role.STAFF) {
            model.addAttribute("profileType", "Nhan vien");
            model.addAttribute("profileDescription", "Ho tro van hanh rap va tra cuu thong tin dat ve.");
            model.addAttribute("primaryActionUrl", "/admin/bookings");
            model.addAttribute("primaryActionLabel", "Xem danh sach dat ve");
        } else {
            model.addAttribute("profileType", "Khach hang");
            model.addAttribute("profileDescription", "Quan ly thong tin ca nhan va lich su dat ve.");
            model.addAttribute("primaryActionUrl", "/booking/history");
            model.addAttribute("primaryActionLabel", "Xem lich su dat ve");
        }
    }
}
