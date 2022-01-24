package nl.saad.scrabble.server.model;


public class Tile {
    private char type;
    private int score;

    public Tile(char type, int score) {
        this.type = type;
        this.score = score;
    }
    
    public char getType() {
        return type;
    }

    public int getScore() {
        return score;
    }

    public void setType(char type) throws Exception {
        if (!Character.isLetter(type)) {
            throw new Exception("Invalid type.");
        }
        this.type = type;
    }
    
    public void setScore(int score) throws Exception {
        if (score < 0) {
            throw new Exception("Invalid score.");
        }
        this.score = score;
    }

    public boolean equals(Tile tile) { return tile.getType() == this.type && tile.getScore() == this.score; }
    
    @Override
    public String toString() { return Character.toString(type); }

}