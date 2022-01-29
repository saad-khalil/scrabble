package nl.saad.scrabble.server.model;

import nl.saad.scrabble.protocol.Protocol;

public class Word {
    private final String letters;
    private final int length;
    private final int row;
    private final int col;
    private final char direction;

    
    public Word(int r, int c, char direction, String letters) {
        this.letters = letters.toUpperCase().trim();
        this.length = letters.length();
        this.row = r;
        this.col = c;
        this.direction = Character.toUpperCase(direction);
    }
    
    public String getLetters() { return letters; }

    public int getCol() { return col; }

    public int getRow() { return row; }

    public char getDirection() { return direction; }

    public int getLength() { return length; }
    
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

    public String toString() {
        return letters;
    }

}