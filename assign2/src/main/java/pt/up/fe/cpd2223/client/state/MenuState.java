package pt.up.fe.cpd2223.client.state;

import pt.up.fe.cpd2223.common.decoding.Decoder;
import pt.up.fe.cpd2223.common.encoding.Encoder;
import pt.up.fe.cpd2223.common.message.AckMessage;
import pt.up.fe.cpd2223.common.message.EnqueueUserMessage;
import pt.up.fe.cpd2223.common.message.Message;
import pt.up.fe.cpd2223.common.socket.SocketIO;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class MenuState extends State {

    private final long userId;

    public MenuState(Encoder encoder, Decoder decoder, long userId) {
        super(encoder, decoder);
        this.userId = userId;
    }

    private State promptAction(SocketChannel channel) {
        Scanner sc = new Scanner(System.in);

        System.out.println("""
                1. Play
                0. Quit
                """);

        while (true) {
            System.out.print("Option: ");

            int option = sc.nextInt();

            switch (option) {
                case 0 -> {
                    try {
                        channel.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                }
                case 1 -> {

                    var msg = new EnqueueUserMessage(userId);
                    try {
                        SocketIO.write(channel, this.encoder.encode(msg.toFormattedString()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    return new QueueState(this.encoder, this.decoder, userId);
                }
                default -> System.out.printf("Unknown option selected: %d, please select a valid option%n", option);
            }
        }
    }

    @Override
    public State handle(Message message) {

        if (message instanceof AckMessage) {

            var channel = message.getClientSocket();

            return this.promptAction(channel);
        }

        return null;
    }
}
