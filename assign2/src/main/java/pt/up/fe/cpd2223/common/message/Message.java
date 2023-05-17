package pt.up.fe.cpd2223.common.message;

import java.nio.channels.SocketChannel;

public abstract class Message {

    private SocketChannel socket;

    public static Message fromFormattedString(String message) {
        String[] parts = message.split(":");

        int type = Integer.parseInt(parts[0]);

        var messageType = MessageType.from(type);

        return switch (messageType) {
            case AUTH_LOGIN, AUTH_REGISTER -> {
                var payload = parts[1];

                var info = payload.split(Message.payloadDataSeparator());

                String username = info[0], password = info[1];

                yield new LoginMessage(username, password);
            }
            case ACK -> new AckMessage();
            case NACK -> new NackMessage();
            default -> new UnknownMessage();
        };
    }

    public static String payloadDataSeparator() {
        return ";";
    }

    public static String messageDelimiter() {
        return "\n";
    }

    public Message withChannel(SocketChannel channel) {
        this.socket = channel;
        return this;
    }

    public SocketChannel getClientSocket() {
        return this.socket;
    }

    public abstract MessageType type();

    public abstract String payload();

    public String toFormattedString() {
        return "%d:%s%s".formatted(this.type().getIdentifier(), this.payload(), Message.messageDelimiter());
    }
}
