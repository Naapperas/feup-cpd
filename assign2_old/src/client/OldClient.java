package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class OldClient implements Runnable {

    private final String serverHost;
    private final int serverPort;
    private SocketChannel socketChannel;
    private ByteBuffer buffer;

    public OldClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.buffer = ByteBuffer.allocate(1024); // arbitrary buffer size
    }

    public void connect() {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(serverHost, serverPort));
            System.out.println("Connected to server: " + serverHost + ":" + serverPort);

            handleAuthentication();
            // Handle game-playing logic here

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socketChannel != null) {
                    socketChannel.close();
                    System.out.println("Disconnected from server.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleAuthentication() {
        try {
            Scanner userInputScanner = new Scanner(System.in);

            System.out.println("Enter username: ");
            String username = userInputScanner.nextLine();

            System.out.println("Enter password: ");
            String password = userInputScanner.nextLine();

            sendMessage(username);
            sendMessage(password);

            String response = readMessage();
            System.out.println("Server response: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String message) throws IOException {
        buffer.clear();
        buffer.put((message + "\n").getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        while(buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }
    }

    private String readMessage() throws IOException {
        buffer.clear();
        int bytesRead = socketChannel.read(buffer);

        if (bytesRead == -1)
            throw new IOException("Connection closed");

        buffer.flip();
        return StandardCharsets.UTF_8.decode(buffer).toString().trim();
    }

    @Override
    public void run() {
        connect();
    }

}
