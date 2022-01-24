package nl.saad.scrabble.server.model;


public class Slot {
    private final String premiumType;
    private Tile tile = null;

    
    public Slot(String premiumType) { this.premiumType = premiumType; }

    public static boolean isValid(int col, int row) {
        return col >= 0 && col < Board.N && row >= 0 && row < Board.N;
    }

    public String getPremiumType() { return premiumType; }

    public Tile getTile() { return tile; }

    public void setTile(Tile tile) { this.tile = tile; }
    
    public boolean isEmpty() { return getTile() == null; }

    public int getMultiplier() {
        switch (premiumType) {
            case "C":
            case "2W":
            case "2L":
                return 2;
            case "3W":
            case "3L":
                return 3;
            default:
                return 1;
        }
    }
    
    @Override
    public String toString() {
        if (tile == null) {
            return premiumType;
        }
        return tile.toString();
    }

}