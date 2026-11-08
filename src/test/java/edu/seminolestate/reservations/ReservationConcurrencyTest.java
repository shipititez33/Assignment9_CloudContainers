
package edu.seminolestate.reservations;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class ReservationConcurrencyTest {

    @Test
    void onlyOneClientCanReserveLastCamera() throws Exception {
        Path testFile = Files.createTempFile("reservation-test-", ".txt");

        try {
            ReservationService service = new ReservationService(
                    new FileReservationStore(testFile.toString()));

            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);
            AtomicInteger successful = new AtomicInteger();
            AtomicInteger rejected = new AtomicInteger();

            Runnable attempt = () -> {
                ready.countDown();

                try {
                    start.await();
                    service.reserve("CAMERA", "Test Customer", 1);
                    successful.incrementAndGet();
                } catch (IllegalStateException e) {
                    rejected.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            };

            Thread first = new Thread(attempt);
            Thread second = new Thread(attempt);

            first.start();
            second.start();

            ready.await();
            start.countDown();

            first.join();
            second.join();

            assertEquals(1, successful.get());
            assertEquals(1, rejected.get());

            int available = service.getResources().stream()
                    .filter(resource -> resource.getId().equals("CAMERA"))
                    .findFirst()
                    .orElseThrow()
                    .getAvailable();

            assertEquals(0, available);

        } finally {
            Files.deleteIfExists(testFile);
        }
    }
}
