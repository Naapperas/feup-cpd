package pt.up.fe.cpd2223.common.message;

public class PlayerToMoveMessage extends Message {

    private final long playerId;

    public PlayerToMoveMessage(long playerId) {
        this.playerId = playerId;
    }

    public long getPlayerId() {
        return this.playerId;
    }

    @Override
    public MessageType type() {
        return MessageType.PLAYER_TO_MOVE;
    }

    @Override
    public String payload() {
        return "%d".formatted(this.playerId);
    }
}
