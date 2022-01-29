package nl.saad.scrabble.server.model;

import nl.saad.scrabble.protocol.Protocol;

public class Board {
    public static final int N = 15;

    boolean firstMove = true;

    private final Slot[][] board;

    public Board() {
        board = new Slot[N][N]; // create board with NxN slots
        // Initialise multiplier Slots on board
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                board[i][j] = new Slot(Protocol.TEXT_LAYOUT[i][j]);  // premiumTypes = " ", "C", "2L", "3L", "2W", "3W"
            }
        }
    }

    public Slot[][] getBoard() { return board; }

    public String getSlotString(int r, int c) { return board[r][c].toString(); }

    public void setFirstMove(boolean fm) { firstMove = fm; }
    // place tile on given indexes (no checks)
    public void placeTile(int r, int c, Tile tile) { board[r][c].setTile(tile); }

    public boolean isSlotEmpty(int r, int c) { return board[r][c].isEmpty(); }

    public boolean isSlotValidOccupied(int r, int c) {
        return Slot.isValid(r, c) && !isSlotEmpty(r, c);
    }

    public char[][] getCharBoard() {
        char[][] charBoard = new char[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                charBoard[i][j] = getSlotString(i, j).charAt(0);
            }
        }
        return charBoard;
    }

    public boolean isBoardEmpty() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (!board[i][j].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public String isValidPlacement(Word word, Hand hand) { // null when valid, error otherwise
        int r = word.getRow();
        int c = word.getCol();
        int last_r;
        int last_c;

        System.out.println("valid placement slot?");

        // 1. check if valid slot for placement (inside board boundary)
        if (!Slot.isValid(r, c)) {
            return Protocol.Error.E004.getDescription(); // invalid coordinates
        }

        // get word tail idx (last character)
        if (word.getDirection() == 'H') {
            last_r = r;
            last_c = word.getCol() + word.getLength() - 1;
        } else {
            last_c = c;
            last_r = word.getRow() + word.getLength() - 1;
        }


        System.out.println("tail inside?");

        // 2. word's tail must not exceed board boundary
        if (!Slot.isValid(last_r, last_c)) {
            return Protocol.Error.E005.getDescription();
        }

        System.out.println("firstMove: " + firstMove);

        // 3. if first move, then it must cover the centre Slot
        if (firstMove) {
            int center = (N-1)/2;
            System.out.println("on center?");

            if (!word.coversIndex(center, center)) {
                return Protocol.Error.E014.getDescription();
            }
            return null;
        }
        else { // not first move - if  current word placement joins existing word on the board (append / overlap letter)

            r = word.getRow();
            c = word.getCol();
            int WL = word.getLength();

            // FOR HEAD and TAIL letters
            if (word.getDirection() == 'H' && (isSlotValidOccupied(r, c-1) || isSlotValidOccupied(r, c+WL))) return null; //LEFT, RIGHT SIDES for H
            if (word.getDirection() == 'V' && (isSlotValidOccupied(r-1, c) || isSlotValidOccupied(r+WL, c))) return null; //ABOVE, BELOW for V

            // FOR THE REST OF THE WORD
            System.out.println("overlapping word?");
            for (int i = 0; i < WL; i++) { // first letter to last character
                if (isSlotValidOccupied(r, c) && board[r][c].getTile().getLetter() != word.getLetters().charAt(i)) { // OVERLAPPING SLOT, MUST BE SAME FOR EVERY CHARACTER AS WELL
                    return Protocol.Error.E005.getDescription();
                }
                // AT LEAST ONE NEIGHBORING SLOTS MUST BE OCCUPIED IF VALID
                if (word.getDirection() == 'H') { // HORIZONTAL
                    if ( isSlotValidOccupied(r-1, c) || isSlotValidOccupied(r+1, c)) return  null;  // TOP, BOTTOM
                    c++;
                }
                else { // VERTICAL
                    if ( isSlotValidOccupied(r, c-1) || isSlotValidOccupied(r, c+1)) return null; // LEFT, RIGHT
                    r++;
                }
            }
            return Protocol.Error.E011.getDescription();
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