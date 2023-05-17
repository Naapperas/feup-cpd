package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import app.Main;
import common.decoding.Decoder;
import common.decoding.UTF8Decoder;
import common.message.Message;

public class Server implements Main.Application {

    private final ExecutorService threadPool;

    private Selector channelSelector;

    private final int port;
    private boolean accepting = true; // TODO: figure out when to change this

    private final Decoder stringDecoder;

    private Server(int port, int macConcurrentGames) {
        this.threadPool = Executors.newFixedThreadPool(macConcurrentGames);
        this.port = port;
        this.stringDecoder = new UTF8Decoder();
    }

    public static Server configure(String[] args) {

        // assume that we have the correct amount of arguments, if not throw an error
        if (args.length < 2) {
            return null;
        }

        // we might have errors parsing the input since it comes from the user, sanitize it
        try {
            int port = Integer.parseInt(args[0]);

            // negative ports throw, 0 triggers a bind to an ephemeral port
            if (port <= 0) return null;

            int maxConcurrentGames = Integer.parseInt(args[1]);

            // negative values are invalid
            if (maxConcurrentGames < 0) return null;

            return new Server(port, maxConcurrentGames);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void run() {

        System.out.printf("Starting server on port %d%n", this.port);

        try (ServerSocketChannel serverSocket = ServerSocketChannel.open(); Selector selector = Selector.open()) {

            // since we use a try-with-resource, we guarantee that the server socket will be closed
            this.channelSelector = selector;

            serverSocket
                    .bind(new InetSocketAddress(this.port)) // bind to localhost:<PORT>
                    .configureBlocking(false) // make it non-blocking
                    .register(this.channelSelector, SelectionKey.OP_ACCEPT); // register a selector for accepting client connections

            while (this.accepting) {
                this.channelSelector.select();

                var selectedKeys = selector.selectedKeys();
                for (var key : selectedKeys) {

                    // register the selector for clients reads, so we can process messages and accept clients in the same thread
                    if (key.isAcceptable()) { // received client connection

                        var clientSocket = ((ServerSocketChannel) key.channel()).accept();

                        if (clientSocket == null) continue;

                        System.out.println("Client received: " + clientSocket);

                        clientSocket
                                .configureBlocking(false)
                                .register(this.channelSelector, SelectionKey.OP_READ);

                        // no need to do any more processing, any further communication is handled by the next "else if" clause
                    } else if (key.isReadable()) { // received data on this selector

                        // TODO: bruh

                        this.processReceivedData(key);
                    }
                }
                selectedKeys.clear();
            }
        } catch (IOException exception) {
            System.err.printf("Error occurred while serving clients: %s%n", exception.getMessage());
        }
    }

    public void handleMessage(Message message) {
        System.out.println(message.payload());
    }

    public void processReceivedData(SelectionKey key) throws IOException { // pass in the key to give us more control
        var clientChannel = (SocketChannel) key.channel();

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        int bytesRead = clientChannel.read(buffer);
        if (bytesRead == -1) { // Peer closed socket
            System.out.println("Peer closed their socket endpoint, closing ours.");
            clientChannel.close();
            return;
        }
        buffer.flip();

        Message msg = Message.fromBytes(buffer, stringDecoder);

        handleMessage(msg);
    }
}
