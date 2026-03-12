package com.tournament.service.observer;

import com.tournament.model.Notification;
import com.tournament.model.Registration;
import com.tournament.model.Tournament;
import com.tournament.model.User;
import com.tournament.model.enums.RegistrationStatus;
import com.tournament.repository.NotificationRepository;
import com.tournament.repository.RegistrationRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Observer Pattern: Concrete observer that creates notifications
 * when tournament events occur.
 */
@Component
public class NotificationObserver implements TournamentObserver {

    private final NotificationRepository notificationRepository;
    private final RegistrationRepository registrationRepository;

    public NotificationObserver(NotificationRepository notificationRepository,
                                RegistrationRepository registrationRepository) {
        this.notificationRepository = notificationRepository;
        this.registrationRepository = registrationRepository;
    }

    @Override
    public void onRegistration(User user, Tournament tournament) {
        Notification notification = new Notification(
            "You have successfully registered for tournament: " + tournament.getName(),
            user, tournament
        );
        notificationRepository.save(notification);
    }

    @Override
    public void onMatchScheduled(Tournament tournament) {
        notifyAllParticipants(tournament,
            "Match schedules have been generated for tournament: " + tournament.getName());
    }

    @Override
    public void onResultUpdated(Tournament tournament) {
        notifyAllParticipants(tournament,
            "Match results have been updated for tournament: " + tournament.getName());
    }

    @Override
    public void onTournamentCompleted(Tournament tournament) {
        notifyAllParticipants(tournament,
            "Tournament '" + tournament.getName() + "' has been completed! Check the final standings.");
    }

    private void notifyAllParticipants(Tournament tournament, String message) {
        List<Registration> registrations = registrationRepository
            .findByTournament_TournamentIdAndStatus(tournament.getTournamentId(), RegistrationStatus.APPROVED);
        for (Registration reg : registrations) {
            if (reg.getPlayer() != null) {
                Notification notification = new Notification(message, reg.getPlayer(), tournament);
                notificationRepository.save(notification);
            }
        }
    }
}
