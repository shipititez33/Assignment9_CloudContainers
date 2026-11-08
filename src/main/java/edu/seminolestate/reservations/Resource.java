package edu.seminolestate.reservations;

public class Resource {

    private String id;
    private String name;
    private int available;

    public Resource(String id, String name, int available) {
        this.id = id;
        this.name = name;
        this.available = available;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAvailable() {
        return available;
    }

    public void reduceAvailable(int quantity) {
        available -= quantity;
    }

    public void increaseAvailable(int quantity) {
        available += quantity;
    }
}