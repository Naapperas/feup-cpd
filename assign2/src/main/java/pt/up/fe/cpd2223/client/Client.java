package pt.up.fe.cpd2223.client;

import pt.up.fe.cpd2223.Main;
import pt.up.fe.cpd2223.client.state.LoginState;
import pt.up.fe.cpd2223.client.state.RegisterState;
import pt.up.fe.cpd2223.client.state.State;
import pt.up.fe.cpd2223.common.decoding.Decoder;
import pt.up.fe.cpd2223.common.decoding.UTF8Decoder;
import pt.up.fe.cpd2223.common.encoding.Encoder;
import pt.up.fe.cpd2223.common.encoding.UTF8Encoder;
import pt.up.fe.cpd2223.common.message.MessageReader;
import pt.up.fe.cpd2223.common.message.UnknownMessage;
import pt.up.fe.cpd2223.server.MessageQueue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.Scanner;

public class Client implements Main.Application {

    private final String host;
    private final int port;
    private final Scanner sc = new Scanner(System.in);
    private final Encoder messageEncoder;
    private final Decoder messageDecoder;
    private final MessageQueue messageQueue;
    private SocketChannel channel;

    private boolean stopListening = false;

    private State state;

    private Client(String host, int port) {
        this.host = host;
        this.port = port;
        this.messageEncoder = new UTF8Encoder();
        this.messageDecoder = new UTF8Decoder();
        this.messageQueue = new MessageQueue();

    }

    public static Client create(String[] args) {

        // assume that we have the correct amount of arguments, if not throw an error
        if (args.length < 2) {
            return null;
        }

        // we might have errors parsing the input since it comes from the user, sanitize it
        try {
            String host = args[0];

            int port = Integer.parseInt(args[1]);

            // negative values are invalid
            if (port < 0) return null;

            return new Client(host, port);
        } catch (Exception e) {
            return null;
        }
    }

    public void processMessages() {
        // System.out.println("Client Message Processing Thread started");
        while (!this.stopListening) {
            var message = this.messageQueue.pollMessage(200);

            // make this blocking
            if (message == null) continue;

            this.state = this.state.handle(message);

            if (this.state == null)  {
                try {
                    message.getClientSocket().close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                this.stopListening = true;
            }
        }

        System.out.println("Exiting client message handling thread");
    }

    public State handleAuth() throws IOException {

        Scanner sc = new Scanner(System.in);

        System.out.println("""
                1. Login
                2. Register
                0. Quit
                """);

        while (true) {
            System.out.print("Option: ");

            int option = sc.nextInt();

            switch (option) {
                case 0 -> {
                    this.channel.close();
                    return null;
                }
                case 1 -> {
                    return new LoginState(this.messageEncoder, this.messageDecoder);
                }
                case 2 -> {
                    return new RegisterState(this.messageEncoder, this.messageDecoder);
                }
                default -> System.out.printf("Unknown option selected: %d, please select a valid option%n", option);
            }
        }
    }

    @Override
    public void run() {

        var messageThread = new Thread(this::processMessages);
        messageThread.setDaemon(true);
        messageThread.start();

        try (SocketChannel channel = SocketChannel.open(new InetSocketAddress(this.host, this.port))) {
            // at this point we are connected
            this.channel = channel;

            this.state = this.handleAuth();

            // early termination
            if (this.state == null) return;

            // we guarantee that the state received here can handle this message as an authentication process
            this.state = this.state.handle(new UnknownMessage().withChannel(channel));

            while (channel.isConnected()) {
                try {
                    MessageReader.readMessageToQueue(channel, this.messageDecoder, this.messageQueue);
                } catch (AsynchronousCloseException ignored) {
                }
            }
        } catch (UnresolvedAddressException e) {
            System.err.printf("Failed to connect to server at %s:%d%n", this.host, this.port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
