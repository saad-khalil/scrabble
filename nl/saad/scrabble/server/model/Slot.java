package nl.saad.scrabble.server.model;


public class Slot {
    private final String premiumType;
    private Tile tile = null;

    public Slot(String premiumType) { this.premiumType = premiumType; }

    public static boolean isValid(int r, int c) { return c >= 0 && c < Board.N && r >= 0 && r < Board.N; }

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
            return " ";
        }
        return tile.toString();
    }

}