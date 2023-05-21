package pt.up.fe.cpd2223.common.message;

public class MoveMessage extends Message {

    private final int x, y;
    private final long userId;

    public MoveMessage(int x, int y, long userId) {
        this.x = x;
        this.y = y;
        this.userId = userId;
    }

    public long getUserId() {
        return this.userId;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    @Override
    public MessageType type() {
        return MessageType.MOVE;
    }

    @Override
    public String payload() {
        return "%d%s%d%s%d".formatted(this.userId, Message.payloadDataSeparator(), this.x, Message.payloadDataSeparator(), this.y);
    }
}
