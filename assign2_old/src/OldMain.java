import server.OldServer;
import client.OldClient;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class OldMain {
    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 8080;
        int maxPlayersPerGame = 2;
        int maxConcurrentGames = 5;
        List<SocketChannel> connectedClients = new ArrayList<>();

        OldServer server = new OldServer(port, maxPlayersPerGame, maxConcurrentGames, connectedClients);
        Thread serverThread = new Thread(server);
        serverThread.start();

        // Wait a bit to ensure that the server has time to start up
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        OldClient client = new OldClient(host, port);
        new Thread(client).start();
    }
}
