package pt.up.fe.cpd2223.common.encoding;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class UTF8Encoder implements Encoder {
    @Override
    public ByteBuffer encode(String message) throws IOException {
        return StandardCharsets.UTF_8.encode(message);
    }
}
