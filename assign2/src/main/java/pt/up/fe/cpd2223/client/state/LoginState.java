package pt.up.fe.cpd2223.client.state;

import pt.up.fe.cpd2223.common.decoding.Decoder;
import pt.up.fe.cpd2223.common.encoding.Encoder;
import pt.up.fe.cpd2223.common.message.LoginMessage;
import pt.up.fe.cpd2223.common.message.Message;
import pt.up.fe.cpd2223.common.message.MessageType;
import pt.up.fe.cpd2223.common.socket.SocketIO;

import java.io.IOException;
import java.util.Scanner;

public class LoginState extends State {

    private static final int MAX_RETRIES = 3;
    private int retries = MAX_RETRIES;

    public LoginState(Encoder encoder, Decoder decoder) {
        super(encoder, decoder);
    }

    @Override
    public State handle(Message message) {

        var clientChannel = message.getClientSocket();

        if (this.retries == 0) {
            System.err.printf("Failed to authenticate user after %d retries, closing server connection%n", MAX_RETRIES);
            try {
                clientChannel.close();
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return switch (message.type()) {
            case UNKNOWN, NACK -> {

                if (message.type() == MessageType.NACK)
                    System.out.println("Invalid credentials");

                System.out.printf("%d tries left%n", this.retries--);

                Scanner sc = new Scanner(System.in);

                String username, password;

                System.out.print("Username: ");
                username = sc.nextLine();
                System.out.print("Password: ");
                password = sc.nextLine();

                var msg = new LoginMessage(username, password);

                try {
                    SocketIO.write(clientChannel, this.encoder.encode(msg.toFormattedString()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                yield this;
            }
            case ACK -> new QueueState(this.encoder, this.decoder);
            default -> null;
        };
    }
}
