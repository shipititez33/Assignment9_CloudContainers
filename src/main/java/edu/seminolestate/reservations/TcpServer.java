
package edu.seminolestate.reservations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {

    private final ReservationService service;
    private final int port;

    public TcpServer(ReservationService service, int port) {
        this.service = service;
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("TCP server listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();

                Thread clientThread = new Thread(() -> {
                    try {
                        handleClient(clientSocket);
                    } catch (IOException e) {
                        System.out.println("TCP client disconnected.");
                    }
                });

                clientThread.start();
            }

        } catch (IOException e) {
            System.err.println("TCP server failed: " + e.getMessage());
        }
    }

    private void handleClient(Socket socket) throws IOException {
        try (socket;
             BufferedReader input = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(
                     socket.getOutputStream(), true)) {

            output.println("Connected to Reservation Service.");

            String command;

            while ((command = input.readLine()) != null) {

                try {
                    switch (command.toUpperCase()) {

                        case "LIST":
                            for (Resource resource : service.getResources()) {
                                output.println(
                                        resource.getId() + " - " +
                                        resource.getName() + " - Available: " +
                                        resource.getAvailable()
                                );
                            }
                            output.println("END");
                            break;

                        case "RESERVE":
                            String resourceId = input.readLine();
                            String customerName = input.readLine();
                            String quantityText = input.readLine();

                            if (resourceId == null || customerName == null ||
                                    quantityText == null) {
                                output.println("ERROR Incomplete request.");
                                return;
                            }

                            int quantity = Integer.parseInt(quantityText);

                            Reservation reservation = service.reserve(
                                    resourceId, customerName, quantity
                            );

                            output.println(
                                    "OK Reservation ID: " +
                                    reservation.getReservationId()
                            );
                            break;

                        case "GET":
                            String idText = input.readLine();

                            if (idText == null) {
                                output.println("ERROR Incomplete request.");
                                return;
                            }

                            Reservation found = service.getReservation(
                                    Integer.parseInt(idText)
                            );

                            if (found == null) {
                                output.println("ERROR Reservation not found.");
                            } else {
                                output.println(
                                        "OK " + found.getReservationId() +
                                        " " + found.getResourceId() +
                                        " " + found.getCustomerName() +
                                        " " + found.getQuantity()
                                );
                            }
                            break;

                        case "CANCEL":
                            String cancelText = input.readLine();

                            if (cancelText == null) {
                                output.println("ERROR Incomplete request.");
                                return;
                            }

                            boolean cancelled = service.cancel(
                                    Integer.parseInt(cancelText)
                            );

                            output.println(cancelled
                                    ? "OK Reservation cancelled."
                                    : "ERROR Reservation not found.");
                            break;

                        case "EXIT":
                            output.println("Goodbye.");
                            return;

                        default:
                            output.println("ERROR Unknown command.");
                    }

                } catch (IllegalArgumentException e) {
                    output.println("ERROR Invalid request.");

                } catch (IllegalStateException e) {
                    if ("Not enough availability.".equals(e.getMessage())) {
                        output.println("ERROR Not enough availability.");
                    } else {
                        output.println("ERROR Unable to process request.");
                    }
                }
            }
        }
    }
}
