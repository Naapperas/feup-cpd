package pt.up.fe.cpd2223.common.message;

public class NackMessage extends Message {
    @Override
    public MessageType type() {
        return MessageType.NACK;
    }

    @Override
    public String payload() {
        return null;
    }
}
