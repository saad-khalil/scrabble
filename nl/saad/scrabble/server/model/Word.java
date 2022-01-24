package nl.saad.scrabble.server.model;

import nl.saad.scrabble.protocol.Protocol;

public class Word {
    private final String letters;
    private final int length;
    private final int row;
    private final int col;
    private final char direction;

    
    public Word(String letters, char col, int row, char direction) {
        this.letters = letters.toUpperCase().trim();
        this.length = letters.length();
        this.row = row;
        this.col = col;
        this.direction = Character.toUpperCase(direction);
    }

    public static Word parseMove(String move) {
        String[] moveArgs = move.split(String.valueOf(Protocol.UNIT_SEPARATOR));
        char col = moveArgs[0].charAt(0);
        int row = Integer.parseInt(moveArgs[0].substring(1));
        char direction = moveArgs[1].charAt(0);
        String letters = moveArgs[2];
        return new Word(letters, col, row, direction);
    }
    
    public String getLetters() { return letters; }

    public int getCol() { return col; }

    public int getRow() { return row; }

    public char getDirection() { return direction; }

    public int getLength() {
        return this.length;
    }
    
    public char charAt(int index) {
        return letters.charAt(index);
    }

    public boolean coversIndex(int r, int c) {
        if (direction == 'H') {
            return row == r && col <= c && col+length-1 >= c;
        } else {
            return col == c && row <= r && row+length-1 >= r;
        }
    }

}