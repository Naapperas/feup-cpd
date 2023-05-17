package pt.up.fe.cpd2223.client;

import pt.up.fe.cpd2223.Main;
import pt.up.fe.cpd2223.common.decoding.Decoder;
import pt.up.fe.cpd2223.common.decoding.UTF8Decoder;
import pt.up.fe.cpd2223.common.encoding.Encoder;
import pt.up.fe.cpd2223.common.encoding.UTF8Encoder;
import pt.up.fe.cpd2223.common.message.LoginMessage;
import pt.up.fe.cpd2223.common.message.Message;
import pt.up.fe.cpd2223.common.message.MessageType;
import pt.up.fe.cpd2223.common.message.RegisterMessage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.Scanner;

public class Client implements Main.Application {

    private SocketChannel channel;

    private final String host;
    private final int port;

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

    public boolean handleLogin(SocketChannel channel) throws IOException {
        System.out.print("Username: ");
        String username = sc.next();
        System.out.print("Password: ");
        String password = sc.next();

        var authMessage = new RegisterMessage(username, password);

        System.out.println(authMessage.toFormattedString());

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

                authenticated = this.handleLogin(channel);

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
