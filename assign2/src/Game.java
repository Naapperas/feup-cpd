import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class Game implements Runnable {
    private final List<Socket> userSockets;
    private final int[][] board = new int[3][3]; // for Tic Tac Toe
    private final int[] scores = new int[2];

    public Game(List<Socket> userSockets) {
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
                Socket currentSocket = userSockets.get(currentPlayer);
                BufferedReader reader = new BufferedReader(new InputStreamReader(currentSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(currentSocket.getOutputStream(), true);

                // Ask for the player's move
                writer.println("Your move: ");
                String move = reader.readLine();
                // Assume move is a string like "1,1" representing the row and column to place their mark

                String[] parts = move.split(",");
                int row = Integer.parseInt(parts[0]);
                int column = Integer.parseInt(parts[1]);

                if (board[row][column] == -1) {
                    // Place the player's move on the board
                    board[row][column] = currentPlayer;

                    // Check if the current player has won
                    if (checkWin(currentPlayer)) {
                        writer.println("Congratulations! You won.");
                        scores[currentPlayer]++;
                        break;
                    } else if (isBoardFull()) {
                        writer.println("The game is a draw.");
                        break;
                    }

                    currentPlayer = 1 - currentPlayer; // Switch player
                } else {
                    writer.println("Invalid move. Try again.");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
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
