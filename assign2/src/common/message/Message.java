package common.message;

import common.decoding.Decoder;
import common.encoding.Encoder;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface Message {

    MessageType type();

    String payload();

    default ByteBuffer toBytes(Encoder encoder) throws IOException {
        String formattedMessage = "%d:%s\n".formatted(this.type().getIdentifier(), this.payload());

        return encoder.encode(formattedMessage);
    }

    static Message fromBytes(ByteBuffer buffer, Decoder decoder) {

        String message = buffer.toString();

        int type = Integer.parseInt(message.split(":")[0]);

        throw new UnsupportedOperationException("Undefined behavior");
    }
}
