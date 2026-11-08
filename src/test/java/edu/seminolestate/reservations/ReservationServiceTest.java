
package edu.seminolestate.reservations;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class ReservationServiceTest {

    private ReservationService createService(Path file) {
        return new ReservationService(
                new FileReservationStore(file.toString()));
    }

    private int cameraAvailability(ReservationService service) {
        return service.getResources().stream()
                .filter(r -> r.getId().equals("CAMERA"))
                .findFirst()
                .orElseThrow()
                .getAvailable();
    }

    @Test
    void reservationReducesAvailability() throws Exception {
        Path file = Files.createTempFile("test-", ".txt");

        try {
            ReservationService service = createService(file);

            Reservation reservation =
                    service.reserve("CAMERA", "Alex", 1);

            assertEquals("CAMERA", reservation.getResourceId());
            assertEquals(1, reservation.getQuantity());
            assertEquals(0, cameraAvailability(service));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void cancellationRestoresAvailability() throws Exception {
        Path file = Files.createTempFile("test-", ".txt");

        try {
            ReservationService service = createService(file);

            Reservation reservation =
                    service.reserve("CAMERA", "Alex", 1);

            service.cancel(reservation.getReservationId());

            assertEquals(1, cameraAvailability(service));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void invalidResourceIsRejected() throws Exception {
        Path file = Files.createTempFile("test-", ".txt");

        try {
            ReservationService service = createService(file);

            assertThrows(IllegalArgumentException.class,
                    () -> service.reserve("INVALID", "Alex", 1));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void zeroQuantityIsRejected() throws Exception {
        Path file = Files.createTempFile("test-", ".txt");

        try {
            ReservationService service = createService(file);

            assertThrows(IllegalArgumentException.class,
                    () -> service.reserve("CAMERA", "Alex", 0));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void negativeQuantityIsRejected() throws Exception {
        Path file = Files.createTempFile("test-", ".txt");

        try {
            ReservationService service = createService(file);

            assertThrows(IllegalArgumentException.class,
                    () -> service.reserve("CAMERA", "Alex", -1));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void overbookingIsRejected() throws Exception {
        Path file = Files.createTempFile("test-", ".txt");

        try {
            ReservationService service = createService(file);

            service.reserve("CAMERA", "Alex", 1);

            assertThrows(IllegalStateException.class,
                    () -> service.reserve("CAMERA", "Jordan", 1));

            assertEquals(0, cameraAvailability(service));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void blankCustomerNameIsRejected() throws Exception {
        Path file = Files.createTempFile("test-", ".txt");

        try {
            ReservationService service = createService(file);

            assertThrows(IllegalArgumentException.class,
                    () -> service.reserve("CAMERA", "  ", 1));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void filePersistenceSurvivesServiceRestart() throws Exception {
        Path file = Files.createTempFile("test-", ".txt");

        try {
            ReservationService first = createService(file);

            Reservation reservation =
                    first.reserve("CAMERA", "Alex", 1);

            ReservationService restarted = createService(file);

            assertEquals(0, cameraAvailability(restarted));
            assertEquals("Alex",
                    restarted.getReservation(
                            reservation.getReservationId())
                            .getCustomerName());
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
