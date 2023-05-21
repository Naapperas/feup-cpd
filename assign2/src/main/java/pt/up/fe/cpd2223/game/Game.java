package pt.up.fe.cpd2223.game;

import pt.up.fe.cpd2223.common.decoding.Decoder;
import pt.up.fe.cpd2223.common.encoding.Encoder;
import pt.up.fe.cpd2223.common.message.*;
import pt.up.fe.cpd2223.common.socket.SocketIO;
import pt.up.fe.cpd2223.server.userQueue.QueueUser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.List;

public class Game {

    private final List<QueueUser> users;

    private final Encoder encoder;
    private final Decoder decoder;

    private int playCount = 0;
    private boolean gameRunning = true;

    private TicTacToe game = new TicTacToe();

    public Game(List<QueueUser> users, Encoder encoder, Decoder decoder) {
        this.users = users;
        this.encoder = encoder;

        this.decoder = decoder;

        try {
            this.broadcastMessage(new GameJoinedMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void broadcastMessage(Message message) throws IOException {
        for (var user : this.users) {
            this.sendMessage(user, message);
        }
    }

    private void sendMessage(QueueUser user, Message message) throws IOException {
        MessageHandler.writeMessage(user.channel(), message, this.encoder);
    }

    public List<QueueUser> getUsers() {
        return this.users;
    }

    private int cyclePlayer() {
        return (this.playCount++) % this.users.size();
    }

    public void run() {

        while (this.gameRunning) {

            int currentPlayerIndex = this.cyclePlayer();
            var currentPlayer = this.getUsers().get(currentPlayerIndex);

            try {
                // signal to every player whose turn it is
                this.broadcastMessage(new PlayerToMoveMessage(currentPlayer.user().id()));

                /////////////////////////////////////////////
                // This is ugly, but I lost the ability to think straight

                ByteBuffer buffer = ByteBuffer.allocate(1024);
                int bytesRead = SocketIO.read(currentPlayer.channel(), buffer);

                if (bytesRead <= 0) {
                    System.out.printf("Player %d did not send a message, skipping turn%n", currentPlayer.user().id());
                    throw new ClosedChannelException(); // make it so error handler catches this
                }

                buffer.flip();

                var stringData = this.decoder.decode(buffer);

                // TODO: what about partial reads ?
                var messages = stringData.split(Message.messageDelimiter());

                if (messages.length > 1) {
                    System.out.println("Warning: received more than one message from player");
                }

                /////////////////////////////////////////////

                for (var message : messages) {
                    Message msg = Message.fromFormattedString(message);

                    if (msg instanceof MoveMessage moveMessage) {
                        // handle game logic
                        this.handle(moveMessage, currentPlayer);
                    }
                }

                this.gameRunning = !this.game.isBoardFull() && !this.game.checkForWin();
            } catch (ClosedChannelException e) {

                // TODO: player that was supposed to move disconnected, signal it

                System.out.println(currentPlayer.user().id() + " left");

                currentPlayerIndex = this.cyclePlayer();
                currentPlayer = this.getUsers().get(currentPlayerIndex);

                try {
                    MessageHandler.writeMessage(currentPlayer.channel(), new NackMessage(), this.encoder);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                this.gameRunning = false;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                // do this here to change players only when there is no connection error

                this.game.changePlayer();
            }
        }
    }

    private void handle(MoveMessage moveMessage, QueueUser currentPlayer) throws IOException {

        var userChannel = moveMessage.getClientSocket();

        int x = moveMessage.getX(), y = moveMessage.getY();

        System.out.printf("Received move message: x=%d, y=%d%n", x, y);

        boolean piecePlaced = this.game.placeMark(y, x);

        if (!piecePlaced) {
            // this move was invalid, signal it

            MessageHandler.writeMessage(userChannel, new NackMessage(), this.encoder);
        } else {

            // echo the message back to every client, so they can update themselves.
            // This does however send the MoveMessage to the player that originated it but no biggie
            this.broadcastMessage(moveMessage);

            if (this.game.checkForWin()) {
                // the current player won

                this.broadcastMessage(new GameWonMessage(currentPlayer.user().id()));
            } else if (this.game.isBoardFull()) {
                // draw

                this.broadcastMessage(new GameDrawMessage());
            }
        }
    }
}
