package common.decoding;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface Decoder {

    String decode(ByteBuffer buffer);

}
