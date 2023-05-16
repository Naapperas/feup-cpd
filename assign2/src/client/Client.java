package client;

import common.encoding.Encoder;
import common.encoding.UTF8Encoder;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import app.Main;

public class Client implements Main.Application {

    private SocketChannel channel;

    private String host;
    private int port;

    private Encoder byteEncoder;

    private Client(String host, int port) {
        this.host = host;
        this.port = port;
        this.byteEncoder = new UTF8Encoder();
    }

    public static Client create(String[] args) {

        // assume that we have the correct amount of arguments, if not throw an error
        if (args.length < 2) {
            return null;
        }

        // we might have errors parsing the input since it comes from the user, sanitize it
        try {
            String host = args[0];

            int port = Integer.parseInt(args[1]);

            // negative values are invalid
            if (port < 0) return null;

            return new Client(host, port);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void run() {

        try (SocketChannel channel = SocketChannel.open(new InetSocketAddress(this.host, this.port))) {

            this.channel = channel;

            ByteBuffer buffer = this.byteEncoder.encode("Hello world");

            while (buffer.hasRemaining())
                this.channel.write(buffer);

        } catch (UnresolvedAddressException e) {
            System.err.printf("Failed to connect to server at %s:%d%n", this.host, this.port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
