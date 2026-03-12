package com.tournament.controller;

import com.tournament.model.User;
import com.tournament.model.enums.TournamentStatus;
import com.tournament.service.TournamentService;
import com.tournament.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final TournamentService tournamentService;

    public AdminController(UserService userService, TournamentService tournamentService) {
        this.userService = userService;
        this.tournamentService = tournamentService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long userCount = userService.getUserCount();
        long tournamentCount = tournamentService.getTournamentCount();
        long ongoingCount = tournamentService.getOngoingTournamentCount();
        long completedMatches = tournamentService.getCompletedMatchCount();

        model.addAttribute("userCount", userCount);
        model.addAttribute("tournamentCount", tournamentCount);
        model.addAttribute("ongoingCount", ongoingCount);
        model.addAttribute("completedMatches", completedMatches);
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            String newStatus = "ACTIVE".equals(user.getStatus()) ? "SUSPENDED" : "ACTIVE";
            userService.updateUserStatus(id, newStatus);
            redirectAttributes.addFlashAttribute("success", "User status updated to " + newStatus);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/tournaments")
    public String viewTournaments(Model model) {
        model.addAttribute("tournaments", tournamentService.findAll());
        return "admin/tournaments";
    }

    @GetMapping("/reports")
    public String viewReports(Model model) {
        long userCount = userService.getUserCount();
        long tournamentCount = tournamentService.getTournamentCount();
        long ongoingCount = tournamentService.getOngoingTournamentCount();
        long completedMatches = tournamentService.getCompletedMatchCount();

        long upcomingCount = tournamentService.findByStatus(TournamentStatus.UPCOMING).size();
        long completedCount = tournamentService.findByStatus(TournamentStatus.COMPLETED).size();

        model.addAttribute("userCount", userCount);
        model.addAttribute("tournamentCount", tournamentCount);
        model.addAttribute("ongoingCount", ongoingCount);
        model.addAttribute("completedMatches", completedMatches);
        model.addAttribute("upcomingCount", upcomingCount);
        model.addAttribute("completedTournaments", completedCount);
        return "admin/reports";
    }
}
