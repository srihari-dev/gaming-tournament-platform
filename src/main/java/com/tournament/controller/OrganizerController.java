package com.tournament.controller;

import com.tournament.model.*;
import com.tournament.model.builder.TournamentBuilder;
import com.tournament.model.enums.*;
import com.tournament.repository.OrganizerRepository;
import com.tournament.service.contracts.OrganizerTournamentOperations;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/organizer")
public class OrganizerController {

    private final OrganizerTournamentOperations tournamentService;
    private final OrganizerRepository organizerRepository;

    public OrganizerController(OrganizerTournamentOperations tournamentService,
                               OrganizerRepository organizerRepository) {
        this.tournamentService = tournamentService;
        this.organizerRepository = organizerRepository;
    }

    private Organizer getCurrentOrganizer(Authentication auth) {
        return organizerRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new IllegalStateException("Organizer not found"));
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        Organizer organizer = getCurrentOrganizer(auth);
        List<Tournament> tournaments = tournamentService.findByOrganizer(organizer.getUserId());
        model.addAttribute("organizer", organizer);
        model.addAttribute("tournaments", tournaments);
        return "organizer/dashboard";
    }

    @GetMapping("/tournaments/create")
    public String createForm(Model model) {
        model.addAttribute("formats", TournamentFormat.values());
        return "organizer/create-tournament";
    }

    @PostMapping("/tournaments/create")
    public String createTournament(@RequestParam String name,
                                   @RequestParam String gameTitle,
                                   @RequestParam TournamentFormat format,
                                   @RequestParam int teamSize,
                                   @RequestParam String registrationStart,
                                   @RequestParam String registrationEnd,
                                   @RequestParam double prizePool,
                                   @RequestParam(required = false, defaultValue = "") String rules,
                                   Authentication auth,
                                   RedirectAttributes redirectAttributes) {
        try {
            Organizer organizer = getCurrentOrganizer(auth);
            TournamentBuilder builder = TournamentBuilder.create()
                .withName(name)
                .withGameTitle(gameTitle)
                .withFormat(format)
                .withTeamSize(teamSize)
                .withRegistrationWindow(LocalDate.parse(registrationStart), LocalDate.parse(registrationEnd))
                .withPrizePool(prizePool)
                .withRules(rules);

            tournamentService.createTournament(organizer, builder);
            redirectAttributes.addFlashAttribute("success", "Tournament created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/organizer/tournaments/create";
        }
        return "redirect:/organizer/dashboard";
    }

    @GetMapping("/tournaments/{id}")
    public String viewTournament(@PathVariable Integer id, Authentication auth, Model model) {
        Organizer organizer = getCurrentOrganizer(auth);
        Tournament tournament = tournamentService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
        List<Registration> registrations = tournamentService.getRegistrations(id);
        List<Match> matches = tournamentService.getMatches(id);

        model.addAttribute("organizer", organizer);
        model.addAttribute("tournament", tournament);
        model.addAttribute("registrations", registrations);
        model.addAttribute("matches", matches);
        return "organizer/tournament-detail";
    }

    @PostMapping("/tournaments/{id}/generate-bracket")
    public String generateBracket(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            tournamentService.generateBracket(id);
            redirectAttributes.addFlashAttribute("success", "Brackets generated successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/organizer/tournaments/" + id;
    }

    @GetMapping("/tournaments/{tournamentId}/matches/{matchId}/result")
    public String resultForm(@PathVariable Integer tournamentId, @PathVariable Integer matchId, Model model) {
        Match match = tournamentService.getMatch(matchId);
        model.addAttribute("match", match);
        model.addAttribute("tournamentId", tournamentId);
        return "organizer/submit-result";
    }

    @PostMapping("/tournaments/{tournamentId}/matches/{matchId}/result")
    public String submitResult(@PathVariable Integer tournamentId,
                               @PathVariable Integer matchId,
                               @RequestParam int scoreA,
                               @RequestParam int scoreB,
                               RedirectAttributes redirectAttributes) {
        try {
            tournamentService.submitResult(matchId, scoreA, scoreB);
            redirectAttributes.addFlashAttribute("success", "Result submitted successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/organizer/tournaments/" + tournamentId;
    }

    @PostMapping("/tournaments/{id}/complete")
    public String completeTournament(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            tournamentService.completeTournament(id);
            redirectAttributes.addFlashAttribute("success", "Tournament completed!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/organizer/tournaments/" + id;
    }

    @GetMapping("/tournaments/{id}/report")
    public String tournamentReport(@PathVariable Integer id, Authentication auth, Model model) {
        Organizer organizer = getCurrentOrganizer(auth);
        Tournament tournament = tournamentService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
        List<Match> matches = tournamentService.getMatches(id);
        List<Registration> registrations = tournamentService.getRegistrations(id);

        long completedMatches = matches.stream()
            .filter(m -> m.getStatus() == MatchStatus.COMPLETED).count();

        model.addAttribute("organizer", organizer);
        model.addAttribute("tournament", tournament);
        model.addAttribute("matches", matches);
        model.addAttribute("registrations", registrations);
        model.addAttribute("completedMatches", completedMatches);
        model.addAttribute("totalMatches", matches.size());
        return "organizer/report";
    }
}
