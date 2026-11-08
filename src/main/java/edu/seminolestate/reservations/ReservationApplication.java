
package edu.seminolestate.reservations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationApplication.class, args);
    }

    @Bean
    public CommandLineRunner startTcpServer(ReservationService service) {
        return args -> {
            TcpServer tcpServer = new TcpServer(service, 8000);

            Thread serverThread = new Thread(
                    tcpServer::start,
                    "tcp-server-thread"
            );

            serverThread.setDaemon(true);
            serverThread.start();
        };
    }
}
