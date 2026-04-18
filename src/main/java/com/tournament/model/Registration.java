package com.tournament.model;

import com.tournament.model.enums.RegistrationStatus;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "registrations")
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer registrationId;

    private LocalDate registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status = RegistrationStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    public Registration() {}

    public Registration(Tournament tournament, Team team, Player player) {
        this.tournament = tournament;
        this.team = team;
        this.player = player;
        this.registrationDate = LocalDate.now();
    }

    void approveByTournamentRules() {
        transitionTo(RegistrationStatus.APPROVED);
    }

    void rejectByTournamentRules() {
        transitionTo(RegistrationStatus.REJECTED);
    }

    private void transitionTo(RegistrationStatus nextStatus) {
        if (nextStatus == null) {
            throw new IllegalArgumentException("Registration status cannot be null");
        }
        if (status == nextStatus) {
            return;
        }

        if (status != RegistrationStatus.PENDING) {
            throw new IllegalStateException("Registration status transition is not allowed");
        }
        if (nextStatus != RegistrationStatus.APPROVED && nextStatus != RegistrationStatus.REJECTED) {
            throw new IllegalStateException("Unsupported registration status transition");
        }

        this.status = nextStatus;
    }

    // Getters and Setters
    public Integer getRegistrationId() { return registrationId; }
    public void setRegistrationId(Integer registrationId) { this.registrationId = registrationId; }

    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }

    public RegistrationStatus getStatus() { return status; }

    public Tournament getTournament() { return tournament; }
    public void setTournament(Tournament tournament) { this.tournament = tournament; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
}
