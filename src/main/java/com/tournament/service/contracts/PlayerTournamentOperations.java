package com.tournament.service.contracts;

import com.tournament.model.*;
import com.tournament.model.enums.TournamentStatus;

import java.util.List;
import java.util.Optional;

public interface PlayerTournamentOperations {
    Optional<Tournament> findById(Integer id);
    List<Tournament> findAll();
    List<Tournament> findByStatus(TournamentStatus status);
    Registration registerSoloPlayer(Integer tournamentId, Player player);
    List<Registration> getPlayerRegistrations(Integer playerId);
    List<Registration> getApprovedRegistrations(Integer tournamentId);
    List<Match> getMatches(Integer tournamentId);
}
