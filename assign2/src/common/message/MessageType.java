package common.message;

public enum MessageType {

    AUTH_LOGIN(1),
    AUTH_REGISTER(2),
    ACK(3),
    NACK(4)
    ;

    private final int identifier;

    MessageType(int identifier) {
        this.identifier = 1 << identifier;
    }

    int getIdentifier() {
        return this.identifier;
    }
}
