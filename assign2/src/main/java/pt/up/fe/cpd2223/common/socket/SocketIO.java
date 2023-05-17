package pt.up.fe.cpd2223.common.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public final class SocketIO {

    public static int write(SocketChannel channel, ByteBuffer buffer) {
        int bytesWritten = 0;
        try {
            bytesWritten = channel.write(buffer);

            if (bytesWritten == -1) {
                System.err.println("Error writing to socket");
            }
        } catch (IOException e) {
            e.printStackTrace();
            bytesWritten = -1;
        }

        return bytesWritten;
    }

    public static int read(SocketChannel channel, ByteBuffer buffer) throws IOException {
        int bytesRead = channel.read(buffer);

        if (bytesRead == -1) {
            channel.close();
        }

        return bytesRead;
    }
}
