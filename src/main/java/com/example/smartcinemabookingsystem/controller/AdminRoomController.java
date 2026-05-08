package com.example.smartcinemabookingsystem.controller;

import com.example.smartcinemabookingsystem.model.Room;
import com.example.smartcinemabookingsystem.service.RoomService;
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
    public String saveRoom(@ModelAttribute Room room, RedirectAttributes redirectAttributes) {
        try {
            roomService.saveRoom(room);
            redirectAttributes.addFlashAttribute("successMessage", "Phòng chiếu đã được lưu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu phòng chiếu: " + e.getMessage());
            if (room.getId() == null) {
                return "redirect:/admin/rooms/new";
            } else {
                return "redirect:/admin/rooms/edit/" + room.getId();
            }
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

    @GetMapping("/delete/{id}")
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
