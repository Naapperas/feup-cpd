package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{
    private static final int MAX_ATTEMPS = 3;
    private final int port;
    private final int maxPlayersPerGame;
    private final int maxConcurrentGames;
    private final ExecutorService gameThreadPool;
    private final Queue<Socket> gameQueue;
    private final Object lock;


    public Server(int port, int maxPlayersPerGame, int maxConcurrentGames, List<Socket> connectedClients) {
        this.port = port;
        this.maxPlayersPerGame = maxPlayersPerGame;
        this.maxConcurrentGames = maxConcurrentGames;
        this.gameThreadPool = Executors.newFixedThreadPool(maxConcurrentGames);
        this.gameQueue = new LinkedList<>();
        this.lock = new Object();
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started on port" + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                synchronized (lock) {
                    gameQueue.offer(clientSocket);
                }

                handleAuthentication(clientSocket);
                handleGameQueue();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGameQueue() {
        if (gameQueue.size() >= maxPlayersPerGame) {
            List<Socket> gamePlayers = new ArrayList<>();

            synchronized (lock) {
                for (int i = 0; i < maxPlayersPerGame; i++) {
                    gamePlayers.add(gameQueue.poll());
                }
            }

            Runnable gameInstance = new Game(gamePlayers);
            gameThreadPool.execute(gameInstance);
        }
    }

    private void handleAuthentication(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

            int attempts = 0;
            boolean isAuthenticated = false;

            while (attempts < MAX_ATTEMPS && ! isAuthenticated) {

                String username = reader.readLine();
                String password = reader.readLine();

                isAuthenticated = authenticateUser(username, password);

                if (isAuthenticated) {
                    writer.println("Authentication successful");
                } else {
                    attempts++;
                    writer.println("Authentication failed. Attempts remaining: " + (3 - attempts));
                }
            }

            if (!isAuthenticated) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean authenticateUser(String username, String password) {
        // TODO: Implement this placeholder logic
        return true;
        //return username.equals("test") && password.equals("test");
    }

}
