package common.message;

public class AckMessage implements Message {
    @Override
    public MessageType type() {
        return MessageType.ACK;
    }

    @Override
    public String payload() {
        return null;
    }
}
