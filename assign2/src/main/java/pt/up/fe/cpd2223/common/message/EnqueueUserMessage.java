package pt.up.fe.cpd2223.common.message;

public class EnqueueUserMessage extends Message {

    private final long userId;

    public EnqueueUserMessage(long userId) {
        this.userId = userId;
    }
    
    @Override
    public MessageType type() {
        return MessageType.ENQUEUE_USER;
    }

    @Override
    public String payload() {
        return "%d".formatted(this.userId);
    }
}
