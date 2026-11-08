
package edu.seminolestate.reservations;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ReservationService {

    private final Map<String, Resource> resources = new LinkedHashMap<>();
    private final ReservationStore store;
    private int nextReservationId = 1;

    public ReservationService(ReservationStore store) {
        this.store = store;

        resources.put("CAMERA", new Resource("CAMERA", "Camera", 1));
        resources.put("LAPTOP", new Resource("LAPTOP", "Laptop", 3));
        resources.put("PROJECTOR", new Resource("PROJECTOR", "Projector", 2));

        // Restore availability and ID numbering from saved reservations.
        for (Reservation reservation : store.findAll()) {
            Resource resource = resources.get(reservation.getResourceId());

            if (resource == null ||
                    reservation.getQuantity() <= 0 ||
                    reservation.getQuantity() > resource.getAvailable()) {
                throw new IllegalStateException("Invalid saved reservation data.");
            }

            resource.reduceAvailable(reservation.getQuantity());
            nextReservationId = Math.max(
                    nextReservationId,
                    reservation.getReservationId() + 1
            );
        }
    }

    public synchronized List<Resource> getResources() {
        List<Resource> result = new ArrayList<>();

        for (Resource resource : resources.values()) {
            result.add(new Resource(
                    resource.getId(),
                    resource.getName(),
                    resource.getAvailable()
            ));
        }

        return result;
    }

    public synchronized Reservation getReservation(int reservationId) {
        return store.findById(reservationId);
    }

    public synchronized Reservation reserve(
            String resourceId, String customerName, int quantity) {

        if (resourceId == null || !resources.containsKey(resourceId)) {
            throw new IllegalArgumentException("Unknown resource.");
        }

        if (customerName == null ||
                customerName.isBlank() ||
                customerName.length() > 100 ||
                customerName.contains("|") ||
                customerName.contains("\n") ||
                customerName.contains("\r")) {
            throw new IllegalArgumentException("Invalid customer name.");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }

        Resource resource = resources.get(resourceId);

        if (quantity > resource.getAvailable()) {
            throw new IllegalStateException("Not enough availability.");
        }

        Reservation reservation = new Reservation(
                nextReservationId, resourceId, customerName, quantity
        );

        // Save first so a storage failure does not reduce availability.
        store.save(reservation);
        resource.reduceAvailable(quantity);
        nextReservationId++;

        CompletableFuture.runAsync(() ->
                System.out.println(
                        "AUDIT: Reservation " +
                        reservation.getReservationId() +
                        " created for " + reservation.getResourceId()
                )
        );

        return reservation;
    }

    public synchronized boolean cancel(int reservationId) {
        Reservation reservation = store.findById(reservationId);

        if (reservation == null) {
            return false;
        }

        // Delete first so a storage failure does not restore availability.
        store.delete(reservationId);

        Resource resource = resources.get(reservation.getResourceId());
        resource.increaseAvailable(reservation.getQuantity());

        CompletableFuture.runAsync(() ->
                System.out.println(
                        "AUDIT: Reservation " + reservationId + " cancelled"
                )
        );

        return true;
    }
}
