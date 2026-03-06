package org.example.service;

import org.example.entity.Reservation;
import java.util.Optional;

public interface ReservationService {
    Reservation save(Reservation reservation);
    Optional<Reservation> findById(Long id);
    void update(Reservation reservation);
    void delete(Reservation reservation);
}