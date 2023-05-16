package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private static final int MAX_ATTEMPS = 3;
    private final int port;
    private final int maxPlayersPerGame;
    private final int maxConcurrentGames;
    private final ExecutorService gameThreadPool;
    private final Queue<SocketChannel> gameQueue;
    private final Object lock;

    private Selector selector;
    private ByteBuffer buffer;

    public Server(int port, int maxPlayersPerGame, int maxConcurrentGames, List<SocketChannel> connectedClients) throws IOException {
        this.port = port;
        this.maxPlayersPerGame = maxPlayersPerGame;
        this.maxConcurrentGames = maxConcurrentGames;
        this.gameThreadPool = Executors.newFixedThreadPool(maxConcurrentGames);
        this.gameQueue = new LinkedList<>();
        this.lock = new Object();
        this.selector = Selector.open();
        this.buffer = ByteBuffer.allocate(1024);
    }

    @Override
    public void run() {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server started on port" + port);

            while (true) {
                selector.select();
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (key.isAcceptable()) {
                        SocketChannel clientSocketChannel = serverSocketChannel.accept();
                        clientSocketChannel.configureBlocking(false);
                        clientSocketChannel.register(selector, SelectionKey.OP_READ);
                        System.out.println("New client connected: " + clientSocketChannel);
                        synchronized (lock) {
                            gameQueue.offer(clientSocketChannel);
                        }
                        handleAuthentication(clientSocketChannel);
                        handleGameQueue();
                    }

                    keyIterator.remove();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGameQueue() {
        if (gameQueue.size() >= maxPlayersPerGame) {
            List<SocketChannel> gamePlayers = new ArrayList<>();

            synchronized (lock) {
                for (int i = 0; i < maxPlayersPerGame; i++) {
                    gamePlayers.add(gameQueue.poll());
                }
            }

            Runnable gameInstance = new Game(gamePlayers);
            gameThreadPool.execute(gameInstance);
        }
    }

    private void handleAuthentication(SocketChannel clientSocketChannel) {
        try {
            int attempts = 0;
            boolean isAuthenticated = false;

            while (attempts < MAX_ATTEMPS && !isAuthenticated) {

                String username = readMessage(clientSocketChannel);
                String password = readMessage(clientSocketChannel);

                isAuthenticated = authenticateUser(username, password);

                if (isAuthenticated) {
                    sendMessage("Authentication successful", clientSocketChannel);
                } else {
                    attempts++;
                    sendMessage("Authentication failed. Attempts remaining: " + (3 - attempts), clientSocketChannel);
                }
            }

            if (!isAuthenticated) {
                clientSocketChannel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String message, SocketChannel socketChannel) throws IOException {
        buffer.clear();
        buffer.put((message + "\n").getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }
    }

    private String readMessage(SocketChannel socketChannel) throws IOException {
        buffer.clear();
        socketChannel.read(buffer);
        buffer.flip();
        return StandardCharsets.UTF_8.decode(buffer).toString();
    }

    private boolean authenticateUser(String username, String password) {
        // TODO: Implement this placeholder logic
        return true;
        //return username.equals("test") && password.equals("test");
    }
}