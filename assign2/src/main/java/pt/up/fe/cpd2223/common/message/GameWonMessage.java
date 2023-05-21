package pt.up.fe.cpd2223.common.message;

public class GameWonMessage extends Message {

    private final long winnerId;

    public GameWonMessage(long winnerId) {
        this.winnerId = winnerId;
    }

    public long getWinnerId() {
        return winnerId;
    }

    @Override
    public MessageType type() {
        return MessageType.GAME_WON;
    }

    @Override
    public String payload() {
        return "%d".formatted(this.winnerId);
    }
}
