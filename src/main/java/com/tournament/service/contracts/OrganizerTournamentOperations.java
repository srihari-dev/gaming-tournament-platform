package com.tournament.service.contracts;

import com.tournament.model.*;
import com.tournament.model.builder.TournamentBuilder;

import java.util.List;
import java.util.Optional;

public interface OrganizerTournamentOperations {
    Tournament createTournament(Organizer organizer, TournamentBuilder builder);
    Optional<Tournament> findById(Integer id);
    List<Tournament> findByOrganizer(Integer organizerId);
    List<Registration> getRegistrations(Integer tournamentId);
    List<Match> getMatches(Integer tournamentId);
    Bracket generateBracket(Integer tournamentId);
    Match getMatch(Integer matchId);
    Result submitResult(Integer matchId, int scoreA, int scoreB);
    void completeTournament(Integer tournamentId);
}
