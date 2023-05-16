package server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Game implements Runnable {
    private final List<SocketChannel> userSockets;
    private final int[][] board = new int[3][3]; // for Tic Tac Toe
    private final int[] scores = new int[2];
    private final ByteBuffer buffer = ByteBuffer.allocate(1024);

    public Game(List<SocketChannel> userSockets) {
        this.userSockets = userSockets;
    }

    @Override
    public void run() {
        // Initialize the board
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = -1;
            }
        }
        playGame();
    }

    private void playGame() {
        int currentPlayer = 0;
        while (!isGameOver()) {
            try {
                SocketChannel currentSocket = userSockets.get(currentPlayer);

                // Ask for the player's move
                sendMessage("Your move: ", currentSocket);
                String move = readMessage(currentSocket);
                // Assume move is a string like "1,1" representing the row and column to place their mark

                String[] parts = move.split(",");
                int row = Integer.parseInt(parts[0]);
                int column = Integer.parseInt(parts[1]);

                if (board[row][column] == -1) {
                    // Place the player's move on the board
                    board[row][column] = currentPlayer;

                    // Check if the current player has won
                    if (checkWin(currentPlayer)) {
                        sendMessage("Congratulations! You won.", currentSocket);
                        scores[currentPlayer]++;
                        break;
                    } else if (isBoardFull()) {
                        sendMessage("The game is a draw.", currentSocket);
                        break;
                    }

                    currentPlayer = 1 - currentPlayer; // Switch player
                } else {
                    sendMessage("Invalid move. Try again.", currentSocket);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String readMessage(SocketChannel socketChannel) throws IOException {
        buffer.clear();
        int bytesRead = socketChannel.read(buffer);
        if (bytesRead == -1) {
            throw new IOException("Connection closed");
        }
        buffer.flip();
        return StandardCharsets.UTF_8.decode(buffer).toString().trim();
    }

    private void sendMessage(String message, SocketChannel socketChannel) throws IOException {
        buffer.clear();
        buffer.put((message + "\n").getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }
    }

    private boolean isGameOver() {
        // Check for win or draw conditions
        return checkWin(0) || checkWin(1) || isBoardFull();
    }

    private boolean isBoardFull() {
        for (int[] row : board) {
            for (int cell : row) {
                if (cell == -1) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkWin(int player) {
        // Check horizontal lines
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == player && board[i][1] == player && board[i][2] == player) {
                return true;
            }
        }
        // Check vertical lines
        for (int i = 0; i < 3; i++) {
            if (board[0][i] == player && board[1][i] == player && board[2][i] == player) {
                return true;
            }
        }

        // Check diagonals
        if (board[0][0] == player && board[1][1] == player && board[2][2] == player)
            return true;

        if (board[0][2] == player && board[1][1] == player && board[2][0] == player)
            return true;

        return false;
    }

    public int[] getScores() {
        return scores;
    }
}


