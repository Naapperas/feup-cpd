package pt.up.fe.cpd2223.game;

import pt.up.fe.cpd2223.common.encoding.Encoder;
import pt.up.fe.cpd2223.common.message.GameJoinedMessage;
import pt.up.fe.cpd2223.common.message.Message;
import pt.up.fe.cpd2223.common.socket.SocketIO;
import pt.up.fe.cpd2223.server.userQueue.QueueUser;

import java.io.IOException;
import java.util.List;

public class Game {

    private final List<QueueUser> users;

    private final Encoder encoder;

    public Game(List<QueueUser> users, Encoder encoder) {
        this.users = users;
        this.encoder = encoder;

        try {
            this.broadcastMessage(new GameJoinedMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void broadcastMessage(Message message) throws IOException {
        for (var user : this.users) {
            SocketIO.write(user.channel(), this.encoder.encode(message.toFormattedString()));
        }
    }

    public void run() {}
}
