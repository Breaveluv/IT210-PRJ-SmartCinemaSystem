package com.example.smartcinemabookingsystem.controller;

import com.example.smartcinemabookingsystem.model.Room;
import com.example.smartcinemabookingsystem.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/rooms")
@RequiredArgsConstructor
public class AdminRoomController {

    private final RoomService roomService;

    @GetMapping
    public String listRooms(Model model) {
        model.addAttribute("rooms", roomService.getAllRooms());
        return "admin/rooms/list";
    }

    @GetMapping("/new")
    public String newRoomForm(Model model) {
        model.addAttribute("room", new Room());
        return "admin/rooms/form";
    }

    @PostMapping("/save")
    public String saveRoom(@Valid @ModelAttribute Room room,
                          org.springframework.validation.BindingResult bindingResult, 
                          org.springframework.ui.Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/rooms/form";
        }

        try {
            roomService.saveRoom(room);
            redirectAttributes.addFlashAttribute("successMessage", "Phòng chiếu đã được lưu thành công!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "admin/rooms/form";
        }
        return "redirect:/admin/rooms";
    }

    @GetMapping("/edit/{id}")
    public String editRoomForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return roomService.getRoomById(id).map(room -> {
            model.addAttribute("room", room);
            return "admin/rooms/form";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy phòng chiếu.");
            return "redirect:/admin/rooms";
        });
    }

    @PostMapping("/delete/{id}")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roomService.deleteRoom(id);
            redirectAttributes.addFlashAttribute("successMessage", "Phòng chiếu đã được xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa phòng chiếu: " + e.getMessage());
        }
        return "redirect:/admin/rooms";
    }
}
