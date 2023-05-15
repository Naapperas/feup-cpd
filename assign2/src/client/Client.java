package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {

    private final String serverHost;
    private final int serverPort;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public Client(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public void connect() {
        try {
            socket = new Socket(serverHost, serverPort);
            System.out.println("Connected to server: " + serverHost + ":" + serverPort);

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            handleAuthentication();
            // Handle game-playing logic here

            socket.close();
            System.out.println("Disconnected from server.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAuthentication() {
        try {
            BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Enter username: ");
            String username = userInputReader.readLine();

            System.out.println("Enter password: ");
            String password = userInputReader.readLine();

            writer.println(username);
            writer.println(password);

            String response = reader.readLine();
            System.out.println("Server response: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        connect();
    }

}
