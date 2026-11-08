package edu.seminolestate.reservations;

import java.util.List;

public interface ReservationStore {

    void save(Reservation reservation);

    void delete(int reservationId);

    Reservation findById(int reservationId);

    List<Reservation> findAll();
}