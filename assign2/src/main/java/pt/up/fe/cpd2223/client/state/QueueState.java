package pt.up.fe.cpd2223.client.state;

import pt.up.fe.cpd2223.common.decoding.Decoder;
import pt.up.fe.cpd2223.common.encoding.Encoder;
import pt.up.fe.cpd2223.common.message.GameJoinedMessage;
import pt.up.fe.cpd2223.common.message.Message;

public class QueueState extends State {

    private final long userId;

    public QueueState(Encoder encoder, Decoder decoder, long userId) {
        super(encoder, decoder);
        this.userId = userId;
    }

    private void promptForGame() {

    }

    @Override
    public State handle(Message message) {

        var channel = message.getClientSocket();

        if (message instanceof GameJoinedMessage gameJoinedMessage) {
            System.out.println("Joined game");
            return new GameState(this.encoder, this.decoder, userId);
        } else {
            return this;
        }
    }
}
