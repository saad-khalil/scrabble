package nl.saad.scrabble.server.model;

import nl.saad.scrabble.protocol.Protocol;

import java.util.ArrayList;


public class Hand {
    public static final int LIMIT = 7;
    private final Bag bag;
    private final ArrayList<Tile> hand;
    
    public Hand(Bag bag) {
        hand = new ArrayList<>();
        this.bag = bag;

        // fill hand first time
        for (int i = 0; i < LIMIT; i++) {
            Tile draw = bag.drawRandomTile();
            hand.add(draw);
        }
    }
    
    public Bag getBag() { return bag; }
    
    public ArrayList<Tile> getHand() { return hand; }

    public void setHand(ArrayList<Tile> hand) {
        this.hand.clear();
        this.hand.addAll(hand);
    }

    public boolean isEmpty() { return hand.isEmpty(); }

    // check if letter is present in tiles
    public boolean hasLetter(char letter) { return getLetterIndex(letter) != -1; }

    public String refill() throws Exception {
        int tilesNeeded = Math.min(bag.size(), LIMIT - hand.size());
        if (bag.isEmpty() || bag.size() < tilesNeeded) { // empty or insufficient tiles
            throw new Exception(Protocol.Error.E012.getDescription());
        }

        StringBuilder newLetters = new StringBuilder();
        for (int i = 0; i < tilesNeeded; i++) {
            Tile draw = bag.drawRandomTile();
            newLetters.append(draw.getLetter());
            hand.add(draw);
        }
        return newLetters.toString();
    }

    // swap tiles based on an input string of letters
    public String swap(String letters) throws Exception {
        if (bag.size() < LIMIT) {
            throw new Exception(Protocol.Error.E007.getDescription());
        }
        for (char letter : letters.toCharArray()) {
            removeTile(letter);
        }
        String newLetters = refill();
        bag.addTilesFromLetters(letters);
        return newLetters;
    }

    // remove tile with letter
    public void removeTile(char letter) throws Exception {
        if (hasLetter(letter)) {
            hand.remove(getLetterIndex(letter));
        } else {
            throw new Exception(Protocol.Error.E008.getDescription());
        }
    }

    // get tile from letter if present
    public Tile getTile(char letter) throws Exception {
        if (hasLetter(letter)) { // if tile with letter found
            return hand.get(getLetterIndex(letter));
        } else {
            throw new Exception(Protocol.Error.E008.getDescription());
        }
    }

    // get index of tile with letter
    private int getLetterIndex(char letter) {
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getLetter() == letter) {
                return i;
            }
        }
        return -1;
    }



}