package com.example;

import com.example.model.Reservation;
import com.example.service.ReservationService;

import javax.persistence.OptimisticLockException;
import java.time.LocalDateTime;

public class ReservationRetryHandler {

    private static final int MAX_RETRIES = 3;
    private final ReservationService service;

    public ReservationRetryHandler(ReservationService service) {
        this.service = service;
    }

    public Reservation modifierAvecRetry(Reservation demande) {
        // On mémorise ce que l'utilisateur veut écrire
        String salle = demande.getSalle();
        LocalDateTime debut = demande.getDateDebut();
        LocalDateTime fin = demande.getDateFin();

        Reservation courante = demande;
        for (int tentative = 1; tentative <= MAX_RETRIES; tentative++) {
            try {
                return service.modifierReservation(courante);
            } catch (OptimisticLockException e) {
                System.out.println("Conflit détecté, tentative de retry " + tentative);

                courante = service.trouverParId(demande.getId());
                courante.setSalle(salle);
                courante.setDateDebut(debut);
                courante.setDateFin(fin);
            }
        }
        throw new OptimisticLockException("Échec après " + MAX_RETRIES + " tentatives");
    }
}