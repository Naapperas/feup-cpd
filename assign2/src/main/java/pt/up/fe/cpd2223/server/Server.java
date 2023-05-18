package pt.up.fe.cpd2223.server;

import pt.up.fe.cpd2223.Main;
import pt.up.fe.cpd2223.common.decoding.Decoder;
import pt.up.fe.cpd2223.common.decoding.UTF8Decoder;
import pt.up.fe.cpd2223.common.encoding.Encoder;
import pt.up.fe.cpd2223.common.encoding.UTF8Encoder;
import pt.up.fe.cpd2223.common.message.*;
import pt.up.fe.cpd2223.common.model.User;
import pt.up.fe.cpd2223.common.socket.SocketIO;
import pt.up.fe.cpd2223.game.Game;
import pt.up.fe.cpd2223.server.repository.UserRepository;
import pt.up.fe.cpd2223.server.service.AuthService;
import pt.up.fe.cpd2223.server.userQueue.NormalUserQueue;
import pt.up.fe.cpd2223.server.userQueue.QueueUser;
import pt.up.fe.cpd2223.server.userQueue.RankedUserQueue;
import pt.up.fe.cpd2223.server.userQueue.UserQueue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server implements Main.Application {

    private final ExecutorService executor;

    private final int port;
    private final Decoder messageDecoder;
    private final Encoder messageEncoder;
    private final MessageQueue messageQueue;
    private final UserQueue userQueue;
    private final UserRepository userRepository;
    private final AuthService authService;
    private Selector channelSelector;
    private boolean accepting = true; // TODO: figure out when to change this

    private final Map<Long, Game> userToGame;

    private Server(int port, int maxConcurrentGames, UserQueue queue) {
        this.executor = Executors.newFixedThreadPool(maxConcurrentGames);
        this.port = port;
        this.messageDecoder = new UTF8Decoder();
        this.messageEncoder = new UTF8Encoder();
        this.messageQueue = new MessageQueue();

        this.userQueue = queue;

        this.userRepository = new UserRepository();
        this.authService = new AuthService(this.userRepository);

        this.userToGame = new HashMap<>();
    }

    public static Server configure(String[] args) {

        // assume that we have the correct amount of arguments, if not throw an error
        if (args.length < 4) {
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

            int lobbySize = Integer.parseInt(args[2]);

            if (lobbySize < 0) return null;

            String queueType = args[3];

            UserQueue queue;
            switch (queueType.toLowerCase()) {
                case "normal" -> queue = new NormalUserQueue(lobbySize);
                case "ranked" -> queue = new RankedUserQueue(lobbySize);
                default -> throw new RuntimeException();
            }

            return new Server(port, maxConcurrentGames, queue);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void run() {

        var messageProcessingThread = new Thread(this::processMessages);
        messageProcessingThread.setDaemon(true);
        messageProcessingThread.start();

        var userQueueThread = new Thread(this::handleUserQueue);
        userQueueThread.setDaemon(true);
        userQueueThread.start();

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
        } finally {
            this.executor.shutdown();
        }
    }

    private void handleUserQueue() {
        while (true) {
            List<QueueUser> gameUsers = this.userQueue.nextUserGroup();

            if (gameUsers == null) continue;

            System.out.println("Lobby found");
            // TODO: instantiate Game

            // need to unregister with the selector in order to change the blocking mode
            gameUsers.forEach((quser) -> {
                var userChannel = quser.channel();

                userChannel.keyFor(this.channelSelector).cancel();
                try {
                    userChannel.configureBlocking(true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            var game = new Game(gameUsers, this.messageEncoder);

            gameUsers.forEach((quser) -> this.userToGame.put(quser.user().id(), game));

            this.executor.submit(game::run);
        }
    }

    private void processMessages() {
        System.out.println("Server Message Processing Thread started");
        while (this.accepting) {
            var message = this.messageQueue.pollMessage(200);

            // make this blocking
            if (message == null) continue;

            // in case the message carries a channel;
            SocketChannel channel = message.getClientSocket();

            System.out.printf("Got message %s%n", message.type().name());

            try {
                switch (message.type()) {
                    case AUTH_LOGIN -> {
                        var loginMessage = (LoginMessage) message;

                        var parts = loginMessage.payload().split(Message.payloadDataSeparator());

                        String username = parts[0], password = parts[1];

                        var user = this.authService.login(username, password);

                        Message msg;
                        if (user != null) {
                            msg = new AckMessage(Collections.singletonMap("id", user.id()));
                            channel.keyFor(this.channelSelector).attach(user);
                        } else {
                            msg = new NackMessage();
                        }

                        SocketIO.write(channel, this.messageEncoder.encode(msg.toFormattedString()));
                    }
                    case AUTH_REGISTER -> {
                        var registerMessage = (RegisterMessage) message;

                        var parts = registerMessage.payload().split(Message.payloadDataSeparator());

                        String username = parts[0], password = parts[1];

                        var user = this.authService.register(username, password);

                        Message msg;
                        if (user != null) {
                            msg = new AckMessage(Collections.singletonMap("id", user.id()));
                            channel.keyFor(this.channelSelector).attach(user);
                        } else {
                            msg = new NackMessage();
                        }

                        SocketIO.write(channel, this.messageEncoder.encode(msg.toFormattedString()));
                    }
                    case ENQUEUE_USER -> {
                        var registerMessage = (EnqueueUserMessage) message;

                        var parts = registerMessage.payload().split(Message.payloadDataSeparator());

                        long userId = Long.parseLong(parts[0]);

                        var user = this.userRepository.findById(userId);

                        if (this.userQueue.addPlayer(new QueueUser(user, channel, new Date(), null)))
                            // add the user to a queue along with its channel
                            System.out.printf("Enqueueing user with id %d%n", user.id());

                    }
                    case USER_DISCONNECTED -> {

//                        var user = (User) channel.keyFor(this.channelSelector).attachment();
//
//                        if (user != null) {
//                            var queuedUser = this.userQueue.getForId(user.id());
//
//                            var newQueuedUser = new QueueUser(queuedUser.user(), queuedUser.channel(), queuedUser.instantJoined(), new Date());
//
//                            this.userQueue.addPlayer(newQueuedUser);
//                        }
                    }
                    default -> {
                    }
                }
            } catch (ClosedChannelException cce) {

                // the channel was closed before we could send a message, nothing we can do with it anymore
                try {
                    channel.close();
                    return;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processReceivedData(SelectionKey key) throws IOException { // pass in the key to give us more control
        var clientChannel = (SocketChannel) key.channel();

        MessageHandler.readMessageToQueue(clientChannel, this.messageDecoder, this.messageQueue);
    }
}
