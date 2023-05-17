package pt.up.fe.cpd2223.common.message;

public class UnknownMessage extends Message {
    @Override
    public MessageType type() {
        return MessageType.UNKNOWN;
    }

    @Override
    public String payload() {
        return null;
    }
}
