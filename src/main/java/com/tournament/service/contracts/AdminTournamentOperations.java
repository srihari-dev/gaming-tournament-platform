package com.tournament.service.contracts;

import com.tournament.model.Tournament;
import com.tournament.model.enums.TournamentStatus;

import java.util.List;

public interface AdminTournamentOperations {
    List<Tournament> findAll();
    List<Tournament> findByStatus(TournamentStatus status);
    long getTournamentCount();
    long getCompletedMatchCount();
    long getOngoingTournamentCount();
}
