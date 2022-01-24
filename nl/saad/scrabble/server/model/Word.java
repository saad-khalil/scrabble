package nl.saad.scrabble.server.model;

import nl.saad.scrabble.protocol.Protocol;

public class Word {
    private final String letters;
    private final int length;
    private final int row;
    private final int col;
    private final char orientation;

    
    public Word(String letters, char col, int row, char orientation) {
        this.letters = letters.toUpperCase().trim();
        this.length = letters.length();
        this.row = row;
        this.col = col;
        this.orientation = Character.toUpperCase(orientation);
    }

    public static Word parseMove(String move) {
        String[] moveArgs = move.split(String.valueOf(Protocol.UNIT_SEPARATOR));
        char col = moveArgs[0].charAt(0);
        int row = Integer.parseInt(moveArgs[0].substring(1));
        char orientation = moveArgs[1].charAt(0);
        String letters = moveArgs[2];
        return new Word(letters, col, row, orientation);
    }
    
    public String getLetters() {
        return letters;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public boolean isHorizontal() {
        return orientation == 'H';
    }

    public boolean isVertical() {
        return orientation == 'V';
    }

    public int getLength() {
        return this.length;
    }
    
    public char charAt(int index) {
        return letters.charAt(index);
    }
    
    public boolean isAlphaString() {
        return letters.matches("[A-Za-z]+");
    }

    public boolean coversIndex(int r, int c) {
        if (isHorizontal()) {
            return row == r && col <= c && col+length-1 >= c;
        } else {
            return col == c && row <= r && row+length-1 >= r;
        }
    }

}