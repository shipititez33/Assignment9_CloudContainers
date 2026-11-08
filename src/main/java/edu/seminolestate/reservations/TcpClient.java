
package edu.seminolestate.reservations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TcpClient {

    public static void main(String[] args) {

        try (Socket socket = new Socket("localhost", 8000);
             BufferedReader input = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(
                     socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println(input.readLine());

            while (true) {
                System.out.println();
                System.out.println("Commands: LIST, RESERVE, GET, CANCEL, EXIT");
                System.out.print("Enter command: ");

                String command = scanner.nextLine().trim().toUpperCase();
                output.println(command);

                switch (command) {

                    case "LIST":
                        String line;
                        while ((line = input.readLine()) != null &&
                                !line.equals("END")) {
                            System.out.println(line);
                        }
                        break;

                    case "RESERVE":
                        System.out.print("Resource ID: ");
                        output.println(scanner.nextLine());

                        System.out.print("Customer name: ");
                        output.println(scanner.nextLine());

                        System.out.print("Quantity: ");
                        output.println(scanner.nextLine());

                        System.out.println(input.readLine());
                        break;

                    case "GET":
                    case "CANCEL":
                        System.out.print("Reservation ID: ");
                        output.println(scanner.nextLine());

                        System.out.println(input.readLine());
                        break;

                    case "EXIT":
                        System.out.println(input.readLine());
                        return;

                    default:
                        System.out.println(input.readLine());
                }
            }

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }
}
