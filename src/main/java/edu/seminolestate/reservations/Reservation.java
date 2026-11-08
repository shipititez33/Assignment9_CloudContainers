package edu.seminolestate.reservations;

public class Reservation {

    private int reservationId;
    private String resourceId;
    private String customerName;
    private int quantity;

    public Reservation(int reservationId, String resourceId,
            String customerName, int quantity) {
        this.reservationId = reservationId;
        this.resourceId = resourceId;
        this.customerName = customerName;
        this.quantity = quantity;
    }

    public int getReservationId() {
        return reservationId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public int getQuantity() {
        return quantity;
    }
}