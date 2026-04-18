package com.tournament.model;

import com.tournament.model.enums.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tournaments")
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tournamentId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String gameTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TournamentFormat format;

    private int teamSize;

    private LocalDate registrationStart;

    private LocalDate registrationEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TournamentStatus status = TournamentStatus.UPCOMING;

    private double prizePool;

    private String rules;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private Organizer organizer;

    @OneToOne(mappedBy = "tournament", cascade = CascadeType.ALL)
    private Bracket bracket;

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Registration> registrations = new ArrayList<>();

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL)
    private List<Notification> notifications = new ArrayList<>();

    public Tournament() {}

    /**
     * GRASP Creator: Tournament owns and creates registrations for itself,
     * while enforcing registration rules in one cohesive place.
     */
    public Registration createRegistration(Player player, Team team, LocalDate onDate) {
        validateRegistrationRules(player, team, onDate);

        Registration registration = new Registration(this, team, player);
        registration.setRegistrationDate(onDate);
        registration.approveByTournamentRules();
        registrations.add(registration);
        return registration;
    }

    public boolean isRegistrationOpenOn(LocalDate date) {
        return date != null
            && registrationStart != null
            && registrationEnd != null
            && !date.isBefore(registrationStart)
            && !date.isAfter(registrationEnd);
    }

    private void validateRegistrationRules(Player player, Team team, LocalDate onDate) {
        if (player == null) {
            throw new IllegalArgumentException("Player is required for registration");
        }
        if (status != TournamentStatus.UPCOMING) {
            throw new IllegalArgumentException("Registration is only allowed while tournament is upcoming");
        }
        if (!isRegistrationOpenOn(onDate)) {
            throw new IllegalArgumentException("Registration window is closed");
        }
        if (team == null) {
            throw new IllegalArgumentException("Team is required for registration");
        }
        if (team.getMembers() == null || team.getMembers().size() != teamSize) {
            throw new IllegalArgumentException("Team member count must match tournament team size");
        }

        boolean alreadyRegistered = registrations.stream()
            .anyMatch(r -> r.getPlayer() != null
                && r.getPlayer().getUserId() != null
                && r.getPlayer().getUserId().equals(player.getUserId()));
        if (alreadyRegistered) {
            throw new IllegalArgumentException("Player already registered for this tournament");
        }
    }

    // Getters and Setters
    public Integer getTournamentId() { return tournamentId; }
    public void setTournamentId(Integer tournamentId) { this.tournamentId = tournamentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGameTitle() { return gameTitle; }
    public void setGameTitle(String gameTitle) { this.gameTitle = gameTitle; }

    public TournamentFormat getFormat() { return format; }
    public void setFormat(TournamentFormat format) { this.format = format; }

    public int getTeamSize() { return teamSize; }
    public void setTeamSize(int teamSize) { this.teamSize = teamSize; }

    public LocalDate getRegistrationStart() { return registrationStart; }
    public void setRegistrationStart(LocalDate registrationStart) { this.registrationStart = registrationStart; }

    public LocalDate getRegistrationEnd() { return registrationEnd; }
    public void setRegistrationEnd(LocalDate registrationEnd) { this.registrationEnd = registrationEnd; }

    public TournamentStatus getStatus() { return status; }
    public void setStatus(TournamentStatus status) { this.status = status; }

    public double getPrizePool() { return prizePool; }
    public void setPrizePool(double prizePool) { this.prizePool = prizePool; }

    public String getRules() { return rules; }
    public void setRules(String rules) { this.rules = rules; }

    public Organizer getOrganizer() { return organizer; }
    public void setOrganizer(Organizer organizer) { this.organizer = organizer; }

    public Bracket getBracket() { return bracket; }
    public void setBracket(Bracket bracket) { this.bracket = bracket; }

    public List<Registration> getRegistrations() { return registrations; }
    public void setRegistrations(List<Registration> registrations) { this.registrations = registrations; }

    public List<Notification> getNotifications() { return notifications; }
    public void setNotifications(List<Notification> notifications) { this.notifications = notifications; }
}
