package pt.up.fe.cpd2223.common.decoding;

import java.nio.ByteBuffer;

public interface Decoder {

    String decode(ByteBuffer buffer);

}
