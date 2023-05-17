package client;

import app.Main;
import common.decoding.Decoder;
import common.decoding.UTF8Decoder;
import common.encoding.Encoder;
import common.encoding.UTF8Encoder;
import common.message.LoginMessage;
import common.message.Message;
import common.message.MessageType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.Scanner;

public class Client implements Main.Application {

    private SocketChannel channel;

    private String host;
    private int port;

    private final Scanner sc = new Scanner(System.in);

    private final Encoder messageEncoder;
    private final Decoder messageDecoder;

    private Client(String host, int port) {
        this.host = host;
        this.port = port;
        this.messageEncoder = new UTF8Encoder();
        this.messageDecoder = new UTF8Decoder();
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

    public boolean handleAuth(SocketChannel channel) throws IOException {
        System.out.print("Username: ");
        String username = sc.next();
        System.out.print("Password: ");
        String password = sc.next();

        var authMessage = new LoginMessage(username, password);

        channel.write(this.messageEncoder.encode(authMessage.toFormattedString()));

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        channel.read(buffer);
        buffer.flip();

        // handle auth response

        var bufferContent = this.messageDecoder.decode(buffer);

        var msg = Message.fromFormattedString(bufferContent.split(Message.messageDelimiter())[0]);

        return msg.type() == MessageType.ACK;
    }

    @Override
    public void run() {

        try (SocketChannel channel = SocketChannel.open(new InetSocketAddress(this.host, this.port))) {

            // at this point we are connected
            this.channel = channel;

            boolean authenticated = false;
            final byte maxRetries = 3;
            byte retries = maxRetries;

            while (retries-- > 0) {

                authenticated = this.handleAuth(channel);

                if (authenticated) break;
                else {
                    System.out.printf("Failed to authenticate user, %d retries left%n", retries);
                }
            }

            if (!authenticated) {
                System.out.printf("User inserted bad credentials %d times, abort%n", maxRetries);
            } else {
                System.out.println("Authenticated");
            }
        } catch (UnresolvedAddressException e) {
            System.err.printf("Failed to connect to server at %s:%d%n", this.host, this.port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
