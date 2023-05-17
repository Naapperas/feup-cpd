package client;

import app.Main;
import common.encoding.Encoder;
import common.encoding.UTF8Encoder;
import common.message.LoginMessage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.Scanner;

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

    public boolean handleAuth(SocketChannel channel) throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Username: ");
        String username = sc.next();
        System.out.print("Password: ");
        String password = sc.next();
        sc.close();

        var authMessage = new LoginMessage(username, password);

        channel.write(authMessage.toBytes(this.byteEncoder));

        // handle auth response

        return true;
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
                    System.out.printf("Failed to authenticate user, %d retries left%n", maxRetries);
                }
            }

            if (!authenticated) {
                System.out.println("User inserted bad credentials 3 times, abort");
            } else {

            }
        } catch (UnresolvedAddressException e) {
            System.err.printf("Failed to connect to server at %s:%d%n", this.host, this.port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
