package org.example;

import org.example.entity.Reservation;
import org.example.entity.Salle;
import org.example.entity.Utilisateur;
import org.example.service.ReservationService;
import org.example.service.ReservationServiceImpl;

import javax.persistence.EntityManagerFactory;
import javax.persistence.OptimisticLockException;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class ConcurrentReservationSimulator {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("optimistic-locking-demo");

    private static final ReservationService reservationService =
            new ReservationServiceImpl(emf);

    public static void main(String[] args) throws InterruptedException {

        // Initialisation des données
        initData();

        // Simulation sans retry
        System.out.println("\n---Simulation d'un conflit sans retry ---");
        simulateConcurrentReservationConflict();

        // Réinitialisation des données
        initData();

        // Simulation avec retry
        System.out.println("\n--- Simulation d'un conflit avec retry ---");
        simulateConcurrentReservationConflictWithRetry();

        // Fermeture
        emf.close();
    }

    private static void initData() {

        Utilisateur utilisateur1 = new Utilisateur("Ilyass", "oubaba", "oubaba.Ilyass@example.com");
        Utilisateur utilisateur2 = new Utilisateur("Yassin", "Abrdil", "Abrdil.Yassin@example.com");

        Salle salle = new Salle("Salle A1", 40);
        salle.setDescription("Salle de réunion équipée d'un projecteur");

        try {
            javax.persistence.EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();

            em.persist(utilisateur1);
            em.persist(utilisateur2);
            em.persist(salle);

            Reservation reservation = new Reservation(
                    LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),
                    LocalDateTime.now().plusDays(1).withHour(12).withMinute(0),
                    "Réunion d'équipe"
            );

            reservation.setUtilisateur(utilisateur1);
            reservation.setSalle(salle);

            em.persist(reservation);

            em.getTransaction().commit();
            em.close();

            System.out.println("Données initialisées avec succès !");
            System.out.println("Réservation créée : " + reservation);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void simulateConcurrentReservationConflict() throws InterruptedException {

        Optional<Reservation> reservationOpt = reservationService.findById(1L);

        if (!reservationOpt.isPresent()) {
            System.out.println("Réservation non trouvée !");
            return;
        }

        Reservation reservation = reservationOpt.get();
        System.out.println("Réservation récupérée : " + reservation);

        CountDownLatch latch = new CountDownLatch(1);

        Thread thread1 = new Thread(() -> {
            try {

                latch.await();

                Reservation r1 = reservationService.findById(1L).get();
                System.out.println("Thread 1 : version = " + r1.getVersion());

                Thread.sleep(1000);

                r1.setMotif("Réunion modifiée par Thread 1");

                try {
                    reservationService.update(r1);
                    System.out.println("Thread 1 : Mise à jour réussie");
                } catch (OptimisticLockException e) {
                    System.out.println("Thread 1 : Conflit détecté !");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Thread thread2 = new Thread(() -> {
            try {

                latch.await();

                Reservation r2 = reservationService.findById(1L).get();
                System.out.println("Thread 2 : version = " + r2.getVersion());

                r2.setDateDebut(r2.getDateDebut().plusHours(1));
                r2.setDateFin(r2.getDateFin().plusHours(1));

                try {
                    reservationService.update(r2);
                    System.out.println("Thread 2 : Mise à jour réussie");
                } catch (OptimisticLockException e) {
                    System.out.println("Thread 2 : Conflit détecté !");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread1.start();
        thread2.start();

        latch.countDown();

        thread1.join();
        thread2.join();

        Optional<Reservation> finalReservationOpt = reservationService.findById(1L);

        finalReservationOpt.ifPresent(r -> {
            System.out.println("\nÉtat final :");
            System.out.println("ID : " + r.getId());
            System.out.println("Motif : " + r.getMotif());
            System.out.println("Date début : " + r.getDateDebut());
            System.out.println("Date fin : " + r.getDateFin());
            System.out.println("Version : " + r.getVersion());
        });
    }

    private static void simulateConcurrentReservationConflictWithRetry() throws InterruptedException {

        OptimisticLockingRetryHandler retryHandler =
                new OptimisticLockingRetryHandler(reservationService, 3);

        CountDownLatch latch = new CountDownLatch(1);

        Thread thread1 = new Thread(() -> {
            try {
                // Attendre que les deux threads soient prêts
                latch.await();

                retryHandler.executeWithRetry(1L, r -> {

                    System.out.println("Thread 1 : modification motif");

                    r.setMotif("Réunion modifiée par Thread 1");

                    // Simuler un traitement long
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });

            } catch (Exception e) {
                System.out.println("Thread 1 erreur : " + e.getMessage());
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                // Simuler un traitement long
                latch.await();

                retryHandler.executeWithRetry(1L, r -> {

                    System.out.println("Thread 2 : modification dates");

                    r.setDateDebut(r.getDateDebut().plusHours(1));
                    r.setDateFin(r.getDateFin().plusHours(1));
                });

            } catch (Exception e) {
                System.out.println("Thread 2 erreur : " + e.getMessage());
            }
        });

        thread1.start();
        thread2.start();

        latch.countDown();

        thread1.join();
        thread2.join();

        Optional<Reservation> finalReservationOpt = reservationService.findById(1L);

        finalReservationOpt.ifPresent(r -> {

            System.out.println("\nÉtat final avec retry :");
            System.out.println("ID : " + r.getId());
            System.out.println("Motif : " + r.getMotif());
            System.out.println("Date début : " + r.getDateDebut());
            System.out.println("Date fin : " + r.getDateFin());
            System.out.println("Version : " + r.getVersion());
        });
    }
}