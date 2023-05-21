package pt.up.fe.cpd2223.client.state;

import pt.up.fe.cpd2223.common.decoding.Decoder;
import pt.up.fe.cpd2223.common.encoding.Encoder;
import pt.up.fe.cpd2223.common.message.*;
import pt.up.fe.cpd2223.common.socket.SocketIO;
import pt.up.fe.cpd2223.game.TicTacToe;

import java.io.IOException;
import java.util.Scanner;

public class GameState extends State {

    private final long userId;
    private final TicTacToe game;

    public GameState(Encoder encoder, Decoder decoder, long userId) {
        super(encoder, decoder);
        this.userId = userId;
        this.game = new TicTacToe();
    }

    public String promptPlayerMove() {
        System.out.println("Your turn, please enter your move in the format x,y: ");

        Scanner sc = new Scanner(System.in);

        return sc.nextLine();
    }

    @Override
    public State handle(Message message) {

        var clientChannel = message.getClientSocket();

        if (message instanceof PlayerToMoveMessage || message instanceof NackMessage) {

            this.game.printBoard();

            if (message instanceof NackMessage) {
                System.out.println("That position is invalid, try another one");
            }

            // This only works because NACK is an unicast, instead of the usual broadcast that we receive
            long playerId = message instanceof NackMessage ? this.userId : ((PlayerToMoveMessage) message).getPlayerId();

            if (playerId == this.userId) {

                boolean validMove = false;

                int x = -1, y = -1;
                while (!validMove) {
                    var move = this.promptPlayerMove();

                    try {
                        var parts = move.split(",");
                        x = Integer.parseInt(parts[0]);
                        y = Integer.parseInt(parts[1]);
                        validMove = true;
                    } catch (Exception e) {
                        System.err.println("Invalid move format");
                    }
                }

                var msg = new MoveMessage(x, y, userId);

                try {
                    SocketIO.write(clientChannel, this.encoder.encode(msg.toFormattedString()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (message instanceof MoveMessage moveMessage) {

            int x = moveMessage.getX(), y = moveMessage.getY();

            boolean markPlaced = this.game.placeMark(y, x);

            if (!markPlaced) {
                System.err.println("Wtf?");
            }

            // this.game.printBoard();
            this.game.changePlayer();
        } else if (message instanceof GameDrawMessage) {
            this.game.printBoard();
            System.out.println("The game ended in a draw :(");

            return new MenuState(this.encoder, this.decoder, this.userId);
        } else if (message instanceof GameWonMessage gwMessage) {

            this.game.printBoard();

            if (gwMessage.getWinnerId() == this.userId) {
                // we won

                System.out.println("Congratulations, you won!");
            } else {
                // we lost

                System.out.println("Too bad, you lose. Better luck next time.");
            }

            return new MenuState(this.encoder, this.decoder, this.userId);
        }

        // default to continuing the game on our part
        return this;
    }
}
