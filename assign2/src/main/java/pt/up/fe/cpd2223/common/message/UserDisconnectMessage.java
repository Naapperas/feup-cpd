package pt.up.fe.cpd2223.common.message;

import java.nio.channels.SocketChannel;

public class UserDisconnectMessage extends Message {
    @Override
    public MessageType type() {
        return MessageType.USER_DISCONNECTED;
    }

    @Override
    public String payload() {
        return null;
    }
}
