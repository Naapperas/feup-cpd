package pt.up.fe.cpd2223.common.encoding;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface Encoder {

    ByteBuffer encode(String message) throws IOException;

}
