package common.encoding;

import java.nio.ByteBuffer;
import java.io.IOException;

public interface Encoder {

    ByteBuffer encode(String message) throws IOException;

}
