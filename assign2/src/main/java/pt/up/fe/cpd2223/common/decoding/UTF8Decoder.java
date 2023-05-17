package pt.up.fe.cpd2223.common.decoding;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class UTF8Decoder implements Decoder {
    @Override
    public String decode(ByteBuffer buffer) {
        return StandardCharsets.UTF_8.decode(buffer).toString();
    }
}
