package pt.up.fe.cpd2223.common.message;

public class AuthenticatedMessage extends Message {

    private final long userId;

    public AuthenticatedMessage(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }

    @Override
    public MessageType type() {
        return MessageType.AUTHENTICATED;
    }

    @Override
    public String payload() {
        return "%d".formatted(this.userId);
    }
}
