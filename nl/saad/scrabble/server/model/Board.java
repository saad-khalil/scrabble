package nl.saad.scrabble.server.model;

public class Board {
    public static final int N = 15;
    public static final String[][] TEXT_LAYOUT = {
            {"3W",  " ",   " ",  "2L",   " ",   " ",   " ",   "3W",  " ",   " ",   " ",  "2L",   " ",   " ",  "3W"},
            {" ",  "2W",   " ",   " ",   " ",  "3L",   " ",   " ",   " ",  "3L",   " ",   " ",   " ",  "2W",   " "},
            {" ",   " ",  "2W",   " ",   " ",   " ",  "2L",   " ",  "2L",   " ",   " ",   " ",  "2W",   " ",   " "},
            {"2L",  " ",   " ",  "2W",   " ",   " ",   " ",  "2L",   " ",   " ",   " ",  "2W",   " ",   " ",  "2L"},
            {" ",   " ",   " ",   " ",  "2W",   " ",   " ",   " ",   " ",   " ",  "2W",   " ",   " ",   " ",   " "},
            {" ",  "3L",   " ",   " ",   " ",  "3L",   " ",   " ",   " ",  "3L",   " ",   " ",   " ",  "3L",   " "},
            {" ",   " ",  "2L",   " ",   " ",   " ",  "2L",   " ",  "2L",   " ",   " ",   " ",  "2L",   " ",   " "},
            {"3W",  " ",   " ",  "2L",   " ",   " ",   " ",   "C",   " ",   " ",   " ",  "2L",   " ",   " ",  "3W"},
            {" ",   " ",  "2L",   " ",   " ",   " ",  "2L",   " ",  "2L",   " ",   " ",   " ",  "2L",   " ",   " "},
            {" ",  "3L",   " ",   " ",   " ",  "3L",   " ",   " ",   " ",  "3L",   " ",   " ",   " ",  "3L",   " "},
            {" ",   " ",   " ",   " ",  "2W",   " ",   " ",   " ",   " ",   " ",  "2W",   " ",   " ",   " ",   " "},
            {"2L",  " ",   " ",  "2W",   " ",   " ",   " ",  "2L",   " ",   " ",   " ",  "2W",   " ",   " ",  "2L"},
            {" ",   " ",  "2W",   " ",   " ",   " ",  "2L",   " ",  "2L",   " ",   " ",   " ",  "2W",   " ",   " "},
            {" ",  "2W",   " ",   " ",   " ",  "3L",   " ",   " ",   " ",  "3L",   " ",   " ",   " ",  "2W",   " "},
            {"3W",  " ",   " ",  "2L",   " ",   " ",   " ",   "3W",  " ",   " ",   " ",  "2L",   " ",   " ",  "3W"}
    };

    private final Slot[][] board;

    public Board() {
        board = new Slot[N][N]; // create board with NxN slots
        // Initialise multiplier Slots on board
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                board[i][j] = new Slot(TEXT_LAYOUT[i][j]);  // premiumTypes = " ", "C", "2L", "3L", "2W", "3W"
            }
        }
    }

    public Slot[][] getBoard() { return board; }

    // place tile on given indexes (no checks)
    public void placeTile(int r, int c, Tile tile) { board[r][c].setTile(tile); }

    public boolean isEmpty() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (!board[i][j].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

}