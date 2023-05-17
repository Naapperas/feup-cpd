package pt.up.fe.cpd2223.client.state;

import pt.up.fe.cpd2223.common.decoding.Decoder;
import pt.up.fe.cpd2223.common.encoding.Encoder;
import pt.up.fe.cpd2223.common.message.LoginMessage;
import pt.up.fe.cpd2223.common.message.Message;
import pt.up.fe.cpd2223.common.message.MessageType;
import pt.up.fe.cpd2223.common.message.RegisterMessage;
import pt.up.fe.cpd2223.common.socket.SocketIO;

import java.io.IOException;
import java.util.Scanner;

public class RegisterState extends State {
    public RegisterState(Encoder encoder, Decoder decoder) {
        super(encoder, decoder);
    }

    @Override
    public State handle(Message message) {
        var clientChannel = message.getClientSocket();

        return switch (message.type()) {
            case UNKNOWN, NACK -> {

                if (message.type() == MessageType.NACK)
                    System.out.println("User already exists, try again with different credentials");

                Scanner sc = new Scanner(System.in);

                String username, password;

                System.out.print("Username: ");
                username = sc.nextLine();
                System.out.print("Password: ");
                password = sc.nextLine();

                var msg = new RegisterMessage(username, password);

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
