
package edu.seminolestate.reservations;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @GetMapping("/resources")
    public List<Resource> getResources() {
        return service.getResources();
    }

    @GetMapping("/reservations/{id}")
    public ResponseEntity<?> getReservation(@PathVariable int id) {
        Reservation reservation = service.getReservation(id);

        if (reservation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Reservation not found."));
        }

        return ResponseEntity.ok(reservation);
    }

    @PostMapping("/reservations")
    public ResponseEntity<?> createReservation(
            @RequestBody Map<String, Object> request) {

        try {
            String resourceId = (String) request.get("resourceId");
            String customerName = (String) request.get("customerName");

            // Reject decimals, strings, and out-of-range quantities.
            Object quantityValue = request.get("quantity");

            if (!(quantityValue instanceof Integer)) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "error",
                                "Quantity must be a whole number."
                        ));
            }

            int quantity = (Integer) quantityValue;

            Reservation reservation =
                    service.reserve(resourceId, customerName, quantity);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(reservation);

        } catch (IllegalArgumentException | ClassCastException |
                 NullPointerException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "Invalid reservation request."
                    ));

        } catch (IllegalStateException e) {

            if ("Not enough availability.".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", e.getMessage()));
            }

            throw e;
        }
    }

    @PutMapping("/reservations/{id}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable int id) {

        boolean cancelled = service.cancel(id);

        if (!cancelled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Reservation not found."));
        }

        return ResponseEntity.ok(
                Map.of("message", "Reservation cancelled.")
        );
    }
}
