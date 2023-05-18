package pt.up.fe.cpd2223.common.message;

import pt.up.fe.cpd2223.common.decoding.Decoder;
import pt.up.fe.cpd2223.common.encoding.Encoder;
import pt.up.fe.cpd2223.common.socket.SocketIO;
import pt.up.fe.cpd2223.server.MessageQueue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class MessageHandler {

    public static void readMessageToQueue(SocketChannel channel, Decoder messageDecoder, MessageQueue messageQueue) throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = SocketIO.read(channel, buffer);

        if (bytesRead <= 0) {

            if (bytesRead == -1) {
                messageQueue.enqueueMessage(new UserDisconnectMessage().withChannel(channel));
            }

            // if the number of bytes read is -1, the channel has already been closed. Either way, wr can just return
            return;
        }

        buffer.flip();

        var stringData = messageDecoder.decode(buffer);

        // TODO: what about partial reads ?
        var messages = stringData.split(Message.messageDelimiter());

        for (var message : messages) {
            Message msg = Message.fromFormattedString(message);

            messageQueue.enqueueMessage(msg.withChannel(channel));
        }
    }

    public static void writeMessage(SocketChannel channel, Message message, Encoder encoder) {
        try {
            SocketIO.write(channel, encoder.encode(message.toFormattedString()));
        } catch (Exception e) {
            System.err.println("Error writing message to channel");
        }
    }
}
