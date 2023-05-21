package pt.up.fe.cpd2223.common.message;

public class GameDrawMessage extends Message {
    @Override
    public MessageType type() {
        return MessageType.GAME_DRAW;
    }

    @Override
    public String payload() {
        return null;
    }
}
