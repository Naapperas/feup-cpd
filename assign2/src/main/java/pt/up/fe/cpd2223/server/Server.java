package pt.up.fe.cpd2223.server;

import pt.up.fe.cpd2223.Main;
import pt.up.fe.cpd2223.common.decoding.Decoder;
import pt.up.fe.cpd2223.common.decoding.UTF8Decoder;
import pt.up.fe.cpd2223.common.encoding.Encoder;
import pt.up.fe.cpd2223.common.encoding.UTF8Encoder;
import pt.up.fe.cpd2223.common.message.*;
import pt.up.fe.cpd2223.server.service.AuthService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class Server implements Main.Application {

    private final ExecutorService executor;
    private final int port;

    private final Decoder messageDecoder;
    private final Encoder messageEncoder;

    private final MessageQueue messageQueue;
    private Selector channelSelector;
    private boolean accepting = true; // TODO: figure out when to change this

    private Server(int port, int macConcurrentGames) {
        this.executor = Executors.newFixedThreadPool(macConcurrentGames);
        this.port = port;
        this.messageDecoder = new UTF8Decoder();
        this.messageEncoder = new UTF8Encoder();
        this.messageQueue = new MessageQueue();
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

        this.executor.submit(this::processMessages);

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
                        this.processReceivedData(key);
                    }
                }
                selectedKeys.clear();
            }
        } catch (IOException exception) {
            System.err.printf("Error occurred while serving clients: %s%n", exception.getMessage());
        }
    }

    public void processMessages() {
        System.out.println("Message Processing Thread started");
        while (true) {
            var message = this.messageQueue.pollMessage();

            // make this blocking
            if (message == null) continue;

            // in case the message carries a channel;
            SocketChannel channel = null;

            System.out.printf("Got message %s%n", message.type().name());

            try {
                switch (message.type()) {
                    case AUTH_LOGIN -> {
                        var loginMessage = (LoginMessage) message;

                        channel = loginMessage.getClientSocket();
                        var parts = loginMessage.payload().split(Message.payloadDataSeparator());

                        String username = parts[0], password = parts[1];

                        var user = AuthService.login(username, password);

                        Message msg;
                        if (user != null) {
                            msg = new AckMessage();
                        } else {
                            msg = new NackMessage();
                        }

                        channel.write(this.messageEncoder.encode(msg.toFormattedString()));
                    }
                    case AUTH_REGISTER -> {
                        var registerMessage = (RegisterMessage) message;

                        channel = registerMessage.getClientSocket();
                        var parts = registerMessage.payload().split(Message.payloadDataSeparator());

                        String username = parts[0], password = parts[1];

                        var user = AuthService.register(username, password);

                        Message msg;
                        if (user != null) {
                            msg = new AckMessage();
                        } else {
                            msg = new NackMessage();
                        }

                        channel.write(this.messageEncoder.encode(msg.toFormattedString()));
                    }
                    default -> {
                    }
                }
            } catch (ClosedChannelException cce) {

                // the channel was closed before we could send a message, nothing we can do with it anymore
                if (channel != null) {
                    try {
                        channel.close();
                        return;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

        var stringData = this.messageDecoder.decode(buffer);

        // TODO: what about partial reads ?
        var messages = stringData.split("\n");

        for (var message : messages) {
            Message msg = Message.fromFormattedString(message);

            this.messageQueue.enqueueMessage(msg.withChannel(clientChannel));
        }
    }
}
