package pt.up.fe.cpd2223.client.state;

import pt.up.fe.cpd2223.common.decoding.Decoder;
import pt.up.fe.cpd2223.common.encoding.Encoder;
import pt.up.fe.cpd2223.common.message.Message;

public class QueueState extends State {
    public QueueState(Encoder encoder, Decoder decoder) {
        super(encoder, decoder);
    }

    @Override
    public State handle(Message message) {

        System.out.println("QueueState");

        return this;
    }
}
