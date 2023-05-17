package pt.up.fe.cpd2223.common.message;

public class AckMessage extends Message {
    @Override
    public MessageType type() {
        return MessageType.ACK;
    }

    @Override
    public String payload() {
        return null;
    }
}
