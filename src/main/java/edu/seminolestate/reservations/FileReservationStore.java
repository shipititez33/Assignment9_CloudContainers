
package edu.seminolestate.reservations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileReservationStore implements ReservationStore {

    private final Path filePath;

    public FileReservationStore(String fileName) {
        filePath = Path.of(fileName);

        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }

            if (Files.notExists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize reservation file.", e);
        }
    }

    @Override
    public synchronized void save(Reservation reservation) {
        List<Reservation> reservations = findAll();
        reservations.add(reservation);
        writeAll(reservations);
    }

    @Override
    public synchronized void delete(int reservationId) {
        List<Reservation> reservations = findAll();
        reservations.removeIf(r -> r.getReservationId() == reservationId);
        writeAll(reservations);
    }

    @Override
    public synchronized Reservation findById(int reservationId) {
        return findAll().stream()
                .filter(r -> r.getReservationId() == reservationId)
                .findFirst()
                .orElse(null);
    }

    @Override
    public synchronized List<Reservation> findAll() {
        List<Reservation> reservations = new ArrayList<>();

        try {
            for (String line : Files.readAllLines(filePath)) {
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split("\\|", -1);

                if (parts.length != 4) {
                    throw new IllegalStateException("Invalid reservation file format.");
                }

                reservations.add(new Reservation(
                        Integer.parseInt(parts[0]),
                        parts[1],
                        parts[2],
                        Integer.parseInt(parts[3])
                ));
            }
        } catch (IOException | NumberFormatException e) {
            throw new IllegalStateException("Unable to read reservation file.", e);
        }

        return reservations;
    }

    private void writeAll(List<Reservation> reservations) {
        List<String> lines = new ArrayList<>();

        for (Reservation r : reservations) {
            lines.add(
                    r.getReservationId() + "|" +
                    r.getResourceId() + "|" +
                    r.getCustomerName() + "|" +
                    r.getQuantity()
            );
        }

        try {
            Files.write(filePath, lines);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save reservations.", e);
        }
    }
}
