package common.message;

public enum MessageType {

    AUTH_LOGIN(1),
    AUTH_REGISTER(2),
    ACK(3),
    NACK(4),
    UNKNOWN(Integer.MAX_VALUE)
    ;

    private final int identifier;

    MessageType(int identifier) {
        this.identifier = identifier;
    }

    public int getIdentifier() {
        return this.identifier;
    }

    static MessageType from(int typeIdentifier) {
        return switch (typeIdentifier) {
            case 1 -> MessageType.AUTH_LOGIN;
            case 2 -> MessageType.AUTH_REGISTER;
            case 3 -> MessageType.ACK;
            case 4 -> MessageType.NACK;
            default -> MessageType.UNKNOWN;
        };
    }
}