package com.college.smartparking;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
public class ParkingController {
    private final JdbcTemplate jdbc;
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public ParkingController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping("/")
    public String home(Model model) {
        List<Map<String, Object>> slots = jdbc.queryForList("SELECT * FROM parking_slots ORDER BY slot_id");
        int available = (int) slots.stream().filter(s -> "AVAILABLE".equals(s.get("status"))).count();
        model.addAttribute("slots", slots);
        model.addAttribute("available", available);
        model.addAttribute("occupied", slots.size() - available);
        return "index";
    }

    @PostMapping("/book")
    public String book(@RequestParam String vehicleNo, @RequestParam int slotId, RedirectAttributes redirect) {
        String vehicle = vehicleNo.trim().toUpperCase();
        if (vehicle.isEmpty()) {
            redirect.addFlashAttribute("error", "Enter a vehicle number first.");
            return "redirect:/";
        }
        String now = LocalDateTime.now().format(FORMAT);
        int changed = jdbc.update("UPDATE parking_slots SET status='OCCUPIED', vehicle_no=?, booked_at=? WHERE slot_id=? AND status='AVAILABLE'",
                vehicle, now, slotId);
        if (changed == 0) {
            redirect.addFlashAttribute("error", "That slot is no longer available. Refresh and try again.");
            return "redirect:/";
        }
        jdbc.update("INSERT INTO parking_log(slot_id, vehicle_no, action, event_time) VALUES (?, ?, ?, ?)",
                slotId, vehicle, "BOOKED", now);
        redirect.addFlashAttribute("message", "Slot " + slotId + " booked for " + vehicle + ".");
        return "redirect:/";
    }

    @PostMapping("/release/{slotId}")
    public String release(@PathVariable int slotId, RedirectAttributes redirect) {
        Map<String, Object> slot;
        try { slot = jdbc.queryForMap("SELECT * FROM parking_slots WHERE slot_id=?", slotId); }
        catch (Exception ex) {
            redirect.addFlashAttribute("error", "Parking slot was not found.");
            return "redirect:/";
        }
        if (!"OCCUPIED".equals(slot.get("status"))) {
            redirect.addFlashAttribute("error", "This slot is already available.");
            return "redirect:/";
        }
        String now = LocalDateTime.now().format(FORMAT);
        String vehicle = String.valueOf(slot.get("vehicle_no"));
        jdbc.update("UPDATE parking_slots SET status='AVAILABLE', vehicle_no=NULL, booked_at=NULL WHERE slot_id=? AND status='OCCUPIED'", slotId);
        jdbc.update("INSERT INTO parking_log(slot_id, vehicle_no, action, event_time) VALUES (?, ?, ?, ?)",
                slotId, vehicle, "RELEASED", now);
        redirect.addFlashAttribute("message", "Slot " + slotId + " released.");
        return "redirect:/";
    }

    @GetMapping("/logs")
    public String logs(Model model) {
        model.addAttribute("logs", jdbc.queryForList("SELECT * FROM parking_log ORDER BY log_id DESC LIMIT 100"));
        return "logs";
    }
}
