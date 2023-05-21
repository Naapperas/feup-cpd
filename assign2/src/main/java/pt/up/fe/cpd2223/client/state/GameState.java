package pt.up.fe.cpd2223.client.state;

import pt.up.fe.cpd2223.common.decoding.Decoder;
import pt.up.fe.cpd2223.common.encoding.Encoder;
import pt.up.fe.cpd2223.common.message.Message;
import pt.up.fe.cpd2223.common.message.MoveMessage;
import pt.up.fe.cpd2223.common.message.PlayerToMoveMessage;
import pt.up.fe.cpd2223.common.socket.SocketIO;

import java.io.IOException;
import java.util.Scanner;

public class GameState extends State {

    private final long userId;

    public GameState(Encoder encoder, Decoder decoder, long userId) {
        super(encoder, decoder);
        this.userId = userId;
    }

    public String promptPlayerMove() {
        System.out.println("Your turn, please enter your move in the format x,y: ");

        Scanner sc = new Scanner(System.in);

        return sc.nextLine();
    }

    @Override
    public State handle(Message message) {

        var clientChannel = message.getClientSocket();

        if (message instanceof PlayerToMoveMessage ptmMessage) {
            if (ptmMessage.getPlayerId() == this.userId) {

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


        }

        // default to continuing the game on our part
        return new GameState(this.encoder, this.decoder, this.userId);
    }
}
