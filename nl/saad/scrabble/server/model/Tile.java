package nl.saad.scrabble.server.model;


public class Tile {
    private char letter;
    private int score;

    public Tile(char letter, int score) {
        this.letter = letter;
        this.score = score;
    }
    
    public char getLetter() {
        return letter;
    }

    public int getScore() {
        return score;
    }

    public void setType(char letter) throws Exception {
        if (!Character.isLetter(letter)) {
            throw new Exception("Invalid letter type.");
        }
        this.letter = letter;
    }
    
    public void setScore(int score) throws Exception {
        if (score < 0) {
            throw new Exception("Invalid score value.");
        }
        this.score = score;
    }

    public boolean equals(Tile tile) { return tile.letter == this.letter && tile.getScore() == this.score; }
    
    @Override
    public String toString() { return Character.toString(letter); }

}