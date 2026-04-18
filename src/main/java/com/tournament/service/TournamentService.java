package com.tournament.service;

import com.tournament.model.*;
import com.tournament.model.builder.TournamentBuilder;
import com.tournament.model.enums.*;
import com.tournament.repository.*;
import com.tournament.service.contracts.AdminTournamentOperations;
import com.tournament.service.contracts.OrganizerTournamentOperations;
import com.tournament.service.contracts.PlayerTournamentOperations;
import com.tournament.service.observer.TournamentObserver;
import com.tournament.service.strategy.BracketGenerationStrategy;
import com.tournament.service.strategy.KnockoutBracketStrategy;
import com.tournament.service.strategy.RoundRobinBracketStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TournamentService implements OrganizerTournamentOperations,
    PlayerTournamentOperations,
    AdminTournamentOperations {

    private final TournamentRepository tournamentRepository;
    private final RegistrationRepository registrationRepository;
    private final BracketRepository bracketRepository;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final ResultRepository resultRepository;

    private final KnockoutBracketStrategy knockoutStrategy;
    private final RoundRobinBracketStrategy roundRobinStrategy;
    private final List<TournamentObserver> observers;

    public TournamentService(TournamentRepository tournamentRepository,
                             RegistrationRepository registrationRepository,
                             BracketRepository bracketRepository,
                             MatchRepository matchRepository,
                             TeamRepository teamRepository,
                             ResultRepository resultRepository,
                             KnockoutBracketStrategy knockoutStrategy,
                             RoundRobinBracketStrategy roundRobinStrategy,
                             List<TournamentObserver> observers) {
        this.tournamentRepository = tournamentRepository;
        this.registrationRepository = registrationRepository;
        this.bracketRepository = bracketRepository;
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.resultRepository = resultRepository;
        this.knockoutStrategy = knockoutStrategy;
        this.roundRobinStrategy = roundRobinStrategy;
        this.observers = observers;
    }

    // ---- Tournament CRUD ----

    public Tournament createTournament(Organizer organizer, TournamentBuilder builder) {
        Tournament tournament = builder.build();
        tournament.setOrganizer(organizer);
        tournament.setStatus(TournamentStatus.UPCOMING);
        return tournamentRepository.save(tournament);
    }

    public Optional<Tournament> findById(Integer id) {
        return tournamentRepository.findById(id);
    }

    public List<Tournament> findAll() {
        return tournamentRepository.findAll();
    }

    public List<Tournament> findByStatus(TournamentStatus status) {
        return tournamentRepository.findByStatus(status);
    }

    public List<Tournament> findByOrganizer(Integer organizerId) {
        return tournamentRepository.findByOrganizer_UserId(organizerId);
    }

    public Tournament updateTournament(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }

    // ---- Registration ----

    public Registration registerPlayer(Integer tournamentId, Player player, Team team) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));

        if (registrationRepository.existsByTournament_TournamentIdAndPlayer_UserId(
                tournamentId, player.getUserId())) {
            throw new IllegalArgumentException("Player already registered for this tournament");
        }

        // Save team if new
        if (team != null && team.getTeamId() == null) {
            team = teamRepository.save(team);
        }

        Registration registration = tournament.createRegistration(player, team, java.time.LocalDate.now());
        Registration saved = registrationRepository.save(registration);

        // Notify observers
        observers.forEach(o -> o.onRegistration(player, tournament));
        return saved;
    }

    public Registration registerSoloPlayer(Integer tournamentId, Player player) {
        // Create a solo team with the player's gamer tag
        Team soloTeam = new Team(player.getGamerTag());
        soloTeam.getMembers().add(player);
        soloTeam = teamRepository.save(soloTeam);
        return registerPlayer(tournamentId, player, soloTeam);
    }

    public List<Registration> getRegistrations(Integer tournamentId) {
        return registrationRepository.findByTournament_TournamentId(tournamentId);
    }

    public List<Registration> getApprovedRegistrations(Integer tournamentId) {
        return registrationRepository.findByTournament_TournamentIdAndStatus(
            tournamentId, RegistrationStatus.APPROVED);
    }

    public List<Registration> getPlayerRegistrations(Integer playerId) {
        return registrationRepository.findByPlayer_UserId(playerId);
    }

    // ---- Bracket Generation (Strategy Pattern) ----

    public Bracket generateBracket(Integer tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));

        // Remove existing bracket if any
        if (tournament.getBracket() != null) {
            bracketRepository.delete(tournament.getBracket());
            tournament.setBracket(null);
            tournamentRepository.save(tournament);
        }

        // Get approved teams
        List<Registration> approved = registrationRepository
            .findByTournament_TournamentIdAndStatus(tournamentId, RegistrationStatus.APPROVED);
        List<Team> teams = approved.stream()
            .map(Registration::getTeam)
            .filter(t -> t != null)
            .distinct()
            .toList();

        if (teams.size() < 2) {
            throw new IllegalArgumentException("Need at least 2 teams to generate brackets");
        }

        // Determine bracket type based on tournament format
        BracketType bracketType = (tournament.getFormat() == TournamentFormat.KNOCKOUT)
            ? BracketType.SINGLE_ELIM : BracketType.LEAGUE;

        Bracket bracket = new Bracket(bracketType, tournament);
        bracket = bracketRepository.save(bracket);

        // Select strategy based on format
        BracketGenerationStrategy strategy = (tournament.getFormat() == TournamentFormat.KNOCKOUT)
            ? knockoutStrategy : roundRobinStrategy;

        List<Match> matches = strategy.generateMatches(bracket, teams);
        for (Match match : matches) {
            matchRepository.save(match);
        }
        bracket.setMatches(matches);

        tournament.setBracket(bracket);
        tournament.setStatus(TournamentStatus.ONGOING);
        tournamentRepository.save(tournament);

        // Notify observers
        observers.forEach(o -> o.onMatchScheduled(tournament));
        return bracket;
    }

    // ---- Match Results ----

    public List<Match> getMatches(Integer tournamentId) {
        return matchRepository.findByBracket_Tournament_TournamentId(tournamentId);
    }

    public Match getMatch(Integer matchId) {
        return matchRepository.findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Match not found"));
    }

    public Result submitResult(Integer matchId, int scoreA, int scoreB) {
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Match not found"));

        if (match.getTeams().size() < 2) {
            throw new IllegalArgumentException("Match does not have two teams assigned");
        }

        Team winner = (scoreA > scoreB) ? match.getTeams().get(0) : match.getTeams().get(1);

        Result result = new Result(scoreA, scoreB, match, winner);
        result = resultRepository.save(result);

        match.setResult(result);
        match.setStatus(MatchStatus.COMPLETED);
        matchRepository.save(match);

        // Notify observers
        Tournament tournament = match.getBracket().getTournament();
        observers.forEach(o -> o.onResultUpdated(tournament));

        return result;
    }

    public void completeTournament(Integer tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
        tournament.setStatus(TournamentStatus.COMPLETED);
        tournamentRepository.save(tournament);
        observers.forEach(o -> o.onTournamentCompleted(tournament));
    }

    // ---- Statistics ----

    public long getTournamentCount() {
        return tournamentRepository.count();
    }

    public long getCompletedMatchCount() {
        return matchRepository.countByStatus(MatchStatus.COMPLETED);
    }

    public long getOngoingTournamentCount() {
        return tournamentRepository.countByStatus(TournamentStatus.ONGOING);
    }
}
