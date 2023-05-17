package common.message;

public class NackMessage implements Message {
    @Override
    public MessageType type() {
        return MessageType.NACK;
    }

    @Override
    public String payload() {
        return null;
    }
}
