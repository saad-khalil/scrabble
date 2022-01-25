package nl.saad.scrabble.server.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Bag { // tiles bank of tiles for every game
    // initial quantities of each tile letter
    public static final Map<Character,Integer> INIT_QUANTITIES = new HashMap<>(){{
        put('A',9); put('B',2); put('C',2); put('D',4);put( 'E',1); put('F',2); put('G',2); put('H',2); put('I',8); put('J',2); put('K',2); put('L',4); put('M',2);
        put('N',6); put('O',8); put('P',2); put('Q',1); put('R',6);  put('S',4); put('T',6); put('U',4); put('V',2); put('W',2); put('X',1); put('Y',2); put('Z',1);  put('!',2);
    }};

    // scores for every tile letter
    public static final Map<Character,Integer> SCORES = new HashMap<>(){{
        put('A',1); put('B',3); put('C',3); put('D',2);  put('E',1); put('F',4); put('G',2); put('H',4); put('I',1); put('J',8); put('K',5); put('L',1); put('M',3);
        put('N',1); put('O',1); put('P',3); put('Q',10); put('R',1); put('S',1); put('T',1); put('U',1); put('V',4); put('W',4); put('X',8); put('Y',4); put('Z',10); put('!',0);
    }};

    private final ArrayList<Tile> tiles;

    public Bag() {
        tiles = new ArrayList<>();

        for (Map.Entry<Character, Integer> pair : INIT_QUANTITIES.entrySet()) {
            Character letter = pair.getKey();
            Integer quantity = pair.getValue(); // corresponding quantity for bag

            // fill bag with required quantity of score/letter tiles
            for (int i = 0; i < quantity; i++) {
                tiles.add(new Tile(letter, SCORES.get(letter)));  // with corresponding score for tile letter
            }
        }
    }

    public ArrayList<Tile> getBag() { return tiles; }

    public boolean isEmpty() { return tiles.isEmpty(); }

    public int size() { return tiles.size(); }

    public void addTile(Tile tile) { tiles.add(tile); }

    // draw random tile from bag
    public Tile drawRandomTile() {
        int rand_idx = (int) (Math.random() * tiles.size());
        Tile t = tiles.get(rand_idx);
        tiles.remove(rand_idx);
        return t;
    }

    // create and add tiles from a letters string given
    public void addTilesFromLetters(String letters) {
        for (int i = 0; i < letters.length(); i++) {
            Character letter = letters.charAt(i);
            tiles.add(new Tile(letter, SCORES.get(letter)));
        }
    }

    public void setTiles(ArrayList<Tile> newBag) {
        tiles.clear();
        tiles.addAll(newBag);
    }
}