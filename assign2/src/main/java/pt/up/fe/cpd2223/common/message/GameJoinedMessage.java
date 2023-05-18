package pt.up.fe.cpd2223.common.message;

public class GameJoinedMessage extends Message {
    @Override
    public MessageType type() {
        return MessageType.GAME_JOINED;
    }

    @Override
    public String payload() {
        return null;
    }
}
