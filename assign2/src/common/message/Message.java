package common.message;

import common.decoding.Decoder;
import common.encoding.Encoder;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface Message {

    static Message fromBytes(ByteBuffer buffer, Decoder decoder) {

        String message = decoder.decode(buffer);

        String[] parts = message.split(":");

        int type = Integer.parseInt(parts[0]);

        var messageType = MessageType.from(type);

        return switch (messageType) {
            case AUTH_LOGIN, AUTH_REGISTER -> {
                var payload = parts[1];

                var info = payload.split(Message.payloadDataSeparator());

                String username = info[0], password = info[1];

                yield new LoginMessage(username, password);
            }
            default -> throw new UnsupportedOperationException("Undefined behavior");
        };
    }

    static String payloadDataSeparator() {
        return ";";
    }

    MessageType type();

    String payload();

    default ByteBuffer toBytes(Encoder encoder) throws IOException {
        String formattedMessage = "%d:%s\n".formatted(this.type().getIdentifier(), this.payload());

        return encoder.encode(formattedMessage);
    }
}
