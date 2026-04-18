package com.tournament.model.builder;

import com.tournament.model.Tournament;
import com.tournament.model.enums.TournamentFormat;
import com.tournament.model.enums.TournamentStatus;

import java.time.LocalDate;

/**
 * Builder Pattern: step-by-step tournament construction for complex setup.
 */
public class TournamentBuilder {

    private String name;
    private String gameTitle;
    private TournamentFormat format;
    private Integer teamSize;
    private LocalDate registrationStart;
    private LocalDate registrationEnd;
    private double prizePool;
    private String rules = "";

    private TournamentBuilder() {
    }

    public static TournamentBuilder create() {
        return new TournamentBuilder();
    }

    public TournamentBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public TournamentBuilder withGameTitle(String gameTitle) {
        this.gameTitle = gameTitle;
        return this;
    }

    public TournamentBuilder withFormat(TournamentFormat format) {
        this.format = format;
        return this;
    }

    public TournamentBuilder withTeamSize(int teamSize) {
        this.teamSize = teamSize;
        return this;
    }

    public TournamentBuilder withRegistrationWindow(LocalDate registrationStart, LocalDate registrationEnd) {
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
        return this;
    }

    public TournamentBuilder withPrizePool(double prizePool) {
        this.prizePool = prizePool;
        return this;
    }

    public TournamentBuilder withRules(String rules) {
        this.rules = (rules == null) ? "" : rules;
        return this;
    }

    public Tournament build() {
        validate();

        Tournament tournament = new Tournament();
        tournament.setName(name);
        tournament.setGameTitle(gameTitle);
        tournament.setFormat(format);
        tournament.setTeamSize(teamSize);
        tournament.setRegistrationStart(registrationStart);
        tournament.setRegistrationEnd(registrationEnd);
        tournament.setPrizePool(prizePool);
        tournament.setRules(rules);
        tournament.setStatus(TournamentStatus.UPCOMING);

        return tournament;
    }

    private void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tournament name is required");
        }
        if (gameTitle == null || gameTitle.isBlank()) {
            throw new IllegalArgumentException("Game title is required");
        }
        if (format == null) {
            throw new IllegalArgumentException("Tournament format is required");
        }
        if (teamSize == null || teamSize < 1) {
            throw new IllegalArgumentException("Team size must be at least 1");
        }
        if (registrationStart == null || registrationEnd == null) {
            throw new IllegalArgumentException("Registration window is required");
        }
        if (registrationEnd.isBefore(registrationStart)) {
            throw new IllegalArgumentException("Registration end date cannot be before start date");
        }
        if (prizePool < 0) {
            throw new IllegalArgumentException("Prize pool cannot be negative");
        }
    }
}
