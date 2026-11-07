package com.example.service;

import com.example.model.Reservation;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.OptimisticLockException;
import javax.persistence.Persistence;
import java.util.List;

public class ReservationServiceImpl implements ReservationService {

    private final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("reservationPU");

    @Override
    public Reservation creerReservation(Reservation reservation) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(reservation);
            em.getTransaction().commit();
            return reservation;
        } finally {
            em.close();
        }
    }

    @Override
    public Reservation modifierReservation(Reservation reservation) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Reservation managed = em.find(Reservation.class, reservation.getId());
            if (managed == null) {
                throw new IllegalArgumentException("Réservation non trouvée");
            }
            // Vérification de la version : c'est le cœur du verrouillage optimiste
            if (managed.getVersion() != reservation.getVersion()) {
                throw new OptimisticLockException(
                        "Version obsolète : attendue " + reservation.getVersion()
                                + ", trouvée en base " + managed.getVersion(), null, managed);
            }
            managed.setSalle(reservation.getSalle());
            managed.setDateDebut(reservation.getDateDebut());
            managed.setDateFin(reservation.getDateFin());
            em.flush();
            em.getTransaction().commit();
            return managed;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Reservation trouverParId(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Reservation.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Reservation> listerReservations() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT r FROM Reservation r", Reservation.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}