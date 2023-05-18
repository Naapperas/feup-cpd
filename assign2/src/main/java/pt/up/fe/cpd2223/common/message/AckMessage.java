package pt.up.fe.cpd2223.common.message;

import java.util.Map;
import java.util.stream.Collectors;

public class AckMessage extends Message {

    private final Map<String, Object> data;

    public AckMessage() {
        this.data = null;
    }

    public AckMessage(Map<String, Object> additionalData) {
        this.data = additionalData;
    }

    public Map<String, Object> data() {
        return this.data;
    }

    @Override
    public MessageType type() {
        return MessageType.ACK;
    }

    @Override
    public String payload() {
        if (this.data == null)
            return null;

        return this.data.entrySet().stream().map((es) -> "%s=%s".formatted(es.getKey(), es.getValue())).collect(Collectors.joining(Message.payloadDataSeparator()));
    }
}
