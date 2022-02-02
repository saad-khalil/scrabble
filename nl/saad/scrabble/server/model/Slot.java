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
    
    @Override
    public String toString() {
        if (tile == null) {
            return " ";
        }
        return tile.toString();
    }

}