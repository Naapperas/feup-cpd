package pt.up.fe.cpd2223.client.state;

import pt.up.fe.cpd2223.common.decoding.Decoder;
import pt.up.fe.cpd2223.common.encoding.Encoder;
import pt.up.fe.cpd2223.common.message.EnqueueUserMessage;
import pt.up.fe.cpd2223.common.message.Message;
import pt.up.fe.cpd2223.common.socket.SocketIO;

import java.io.IOException;

public class QueueState extends State {

    private final long userId;

    public QueueState(Encoder encoder, Decoder decoder, long userId) {
        super(encoder, decoder);
        this.userId = userId;
    }

    @Override
    public State handle(Message message) {

        var channel = message.getClientSocket();

        var msg = new EnqueueUserMessage(userId);

        try {
            SocketIO.write(channel, this.encoder.encode(msg.toFormattedString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this;
    }
}
