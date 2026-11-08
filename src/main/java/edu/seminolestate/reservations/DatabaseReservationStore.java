
package edu.seminolestate.reservations;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseReservationStore implements ReservationStore {

    private final JdbcTemplate jdbc;

    public DatabaseReservationStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private Reservation map(ResultSet rs, int rowNum) throws SQLException {
        return new Reservation(
                rs.getInt("reservation_id"),
                rs.getString("resource_id"),
                rs.getString("customer_name"),
                rs.getInt("quantity")
        );
    }

    @Override
    public void save(Reservation reservation) {
        jdbc.update(
                "insert into reservations " +
                "(reservation_id, resource_id, customer_name, quantity) " +
                "values (?, ?, ?, ?)",
                reservation.getReservationId(),
                reservation.getResourceId(),
                reservation.getCustomerName(),
                reservation.getQuantity()
        );
    }

    @Override
    public void delete(int reservationId) {
        jdbc.update(
                "delete from reservations where reservation_id = ?",
                reservationId
        );
    }

    @Override
    public Reservation findById(int reservationId) {
        return jdbc.query(
                "select * from reservations where reservation_id = ?",
                this::map,
                reservationId
        ).stream().findFirst().orElse(null);
    }

    @Override
    public List<Reservation> findAll() {
        return jdbc.query(
                "select * from reservations order by reservation_id",
                this::map
        );
    }
}
