import server.Server;
import client.Client;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 8080;
        int maxPlayersPerGame = 2;
        int maxConcurrentGames = 5;
        List<SocketChannel> connectedClients = new ArrayList<>();

        Server server = new Server(port, maxPlayersPerGame, maxConcurrentGames, connectedClients);
        Thread serverThread = new Thread(server);
        serverThread.start();

        // Wait a bit to ensure that the server has time to start up
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Client client = new Client(host, port);
        new Thread(client).start();
    }
}
