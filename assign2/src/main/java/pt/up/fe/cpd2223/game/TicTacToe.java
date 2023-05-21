package pt.up.fe.cpd2223.game;

import pt.up.fe.cpd2223.common.message.MoveMessage;

public class TicTacToe {
    private final char[][] board;
    private char currentMark;

    public TicTacToe() {
        board = new char[3][3];
        currentMark = 'x';
        initializeBoard();
    }

    public void initializeBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = '-';
            }
        }
    }

    private void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void printBoard() {
        this.clearConsole();

        System.out.println("-------------");
        for (int i = 0; i < 3; i++) {
            System.out.print("| ");
            for (int j = 0; j < 3; j++) {
                System.out.print(board[i][j] + " | ");
            }
            System.out.println();
            System.out.println("-------------");
        }
    }

    public boolean isBoardFull() {
        boolean isFull = true;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == '-') {
                    isFull = false;
                    break;
                }
            }
        }

        return isFull;
    }

    public boolean checkForWin() {
        return (checkRowsForWin() || checkColumnsForWin() || checkDiagonalsForWin());
    }

    private boolean checkRowsForWin() {
        for (int i = 0; i < 3; i++) {
            if (checkRowCol(board[i][0], board[i][1], board[i][2])) {
                return true;
            }
        }
        return false;
    }

    private boolean checkColumnsForWin() {
        for (int i = 0; i < 3; i++) {
            if (checkRowCol(board[0][i], board[1][i], board[2][i])) {
                return true;
            }
        }
        return false;
    }

    private boolean checkDiagonalsForWin() {
        return ((checkRowCol(board[0][0], board[1][1], board[2][2])) || checkRowCol(board[0][2], board[1][1], board[2][0]));
    }

    private boolean checkRowCol(char c1, char c2, char c3) {

        return ((c1 != '-') && (c1 == c2) && (c2 == c3));
    }

    public boolean changePlayer() {
        if (currentMark == 'x') {
            currentMark = 'o';
            return true;
        } else {
            currentMark = 'x';
            return false;
        }
    }

    public boolean placeMark(int row, int col) {
        if (row >= 0 && col >= 0 && row < 3) {
            if (board[row][col] == '-') {
                board[row][col] = currentMark;
                return true;
            }
        }
        return false;
    }

    public char getCurrentMark() {
        return currentMark;
    }

}