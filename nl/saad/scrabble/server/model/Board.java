package nl.saad.scrabble.server.model;

import nl.saad.scrabble.protocol.Protocol;

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
    boolean firstMove = true;

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

    public String getSlotString(int r, int c) { return board[r][c].toString(); }

    public void setFirstMove(boolean fm) { firstMove = fm; }
    // place tile on given indexes (no checks)
    public void placeTile(int r, int c, Tile tile) { board[r][c].setTile(tile); }

    public boolean isSlotEmpty(int r, int c) { return board[r][c].isEmpty(); }

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


    public boolean isValidPlacement(Word word, Hand hand) {
        int r = word.getRow();
        int c = word.getCol();
        int last_r = -1;
        int last_c = -1;

        // 0. do you even have these required tiles?
        if (!hand.hasTilesFor(word.getLetters())) {
            System.out.println(Protocol.Error.E008.getDescription());
            return false;
        }

        // 1. check if valid slot for placement (inside board boundary)
        if (!Slot.isValid(r, c)) {
            System.out.println(Protocol.Error.E004.getDescription()); // invalid coordinates
            return false;
        }

        // get word tail idx (last character)
        if (word.getDirection() == 'H') {
            last_r = r;
            last_c = word.getCol() + word.getLength() - 1;
        } else {
            last_c = c;
            last_r = word.getRow() + word.getLength() - 1;
        }

        // 2. word's tail must not exceed board boundary
        if (!Slot.isValid(last_r, last_c)) {
            System.out.println(Protocol.Error.E005.getDescription());
            return false;
        }


        // 3. if first move, then it must cover the centre Slot
        if (firstMove) {
            int center = (N-1)/2;
            if (!word.coversIndex(center, center)) {
                System.out.println(Protocol.Error.E014.getDescription());
            }
            return false;
        }
        else { // not first move - Checks if the current word placement would join another existing word on the board (append or overlap letter) -- required rule
            r = word.getRow();
            c = word.getCol();
            for (int i = 0; i < word.getLength(); i++) { // first letter to last character
                if ((Slot.isValid(r, c) && !isSlotEmpty(r, c))) { // overlapping existing letter slot
                    return true;
                }
                if (word.getDirection() == 'H') { // HORIZONTAL
                    if ((Slot.isValid(r-1, c) && !isSlotEmpty(r-1, c))  // TOP
                     || (Slot.isValid(r+1, c) && !isSlotEmpty(r+1, c))){  // BOTTOM
                        return true;
                    }
                    c++;
                }
                else { // VERTICAL
                    if ((Slot.isValid(r, c-1) && !isSlotEmpty(r, c-1))  // LEFT
                     || (Slot.isValid(r, c+1) && !isSlotEmpty(r, c+1))) { // RIGHT
                        return true;
                    }
                    r++;
                }
            }
            return false;
        }
    }

    public void placeWord(Word word, Hand hand) throws Exception {
        int r = word.getRow();
        int c = word.getCol();

        for (int i = 0; i < word.getLength(); i++) {
            char letter = word.charAt(i);

            if (board[r][c].isEmpty()) { // place on non-empty slots along the way
                if (hand.hasLetter(letter)) { // check if in hand
                    placeTile(r, c, hand.getTile(letter)); // remove from hand
                    hand.removeTile(letter);
                } else { // use blank tile otherwise
                    placeTile(r, c, new Tile(letter, 0));
                    hand.removeTile('!');
                }
            }

            if (word.getDirection() == 'H') c++; // traverse horizontally over cols
            else r++; // over rows otherwise
        }
    }



}