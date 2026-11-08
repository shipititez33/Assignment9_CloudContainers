
package edu.seminolestate.reservations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ReservationConfig {

    @Bean
    public ReservationStore reservationStore(
            @Value("${reservation.storage:file}") String storage,
            JdbcTemplate jdbc) {

        if ("file".equalsIgnoreCase(storage)) {
            return new FileReservationStore("data/reservations.txt");
        }

        if ("database".equalsIgnoreCase(storage)) {
            return new DatabaseReservationStore(jdbc);
        }

        throw new IllegalArgumentException(
                "Unsupported reservation storage mode: " + storage
        );
    }

    @Bean
    public ReservationService reservationService(ReservationStore store) {
        return new ReservationService(store);
    }
}
