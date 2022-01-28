package nl.saad.scrabble.server.controller;

import nl.saad.scrabble.protocol.Protocol;
import nl.saad.scrabble.server.model.*;

import java.lang.reflect.Array;
import java.util.*;


public class GameController { // all game helpers necessary
    private Bag bag;
    private Board board;
    private HashMap<Integer, Player> players;
    private ArrayList<Integer> playerOrder;
    private String drawnTiles;
    private int turn;
    private int numPlayers;
    private ScrabbleWordChecker wordChecker;
    private boolean gameRunning;
    private String turnMove;
    private int turnScore;
    public static final Set<String> curr_words = new HashSet<String>();


    public GameController() {
        playerOrder = new ArrayList<Integer>();
        players = new HashMap<>();
        wordChecker = new FileStreamScrabbleWordChecker();
    }

    public void startGame() {
        bag = new Bag();
        board = new Board();
        curr_words.clear();
        drawnTiles = "";
        turnMove = "";
        turnScore = 0;
        turn = 0;
        for (Integer pID : playerOrder) {
            players.put(pID, new Player(new Hand(bag)));
        }

        board.setFirstMove(true);
        gameRunning = true;
    }


    public int getTurnPlayerID() {
        if (playerOrder.isEmpty()) {
            return -1;
        }

        return playerOrder.get(turn % numPlayers);
    }

    public String getPlayerHandLetters(int pID) {
        return players.get(pID).getHand().getLetters();
    }

    public int getPlayerScore(int pID) {
        return players.get(pID).getScore();
    }

    public void nextTurn() {
        turn++;
        board.setFirstMove(false);
    }


    public String getDrawnTiles() { return drawnTiles; }

    public String getTurnMove() { return turnMove; }

    public int getTurnScore() { return turnScore; }

    public Board getBoard() { return board; }

    public boolean isGameRunning() { return gameRunning; }

    public void setGameRunning(boolean running) { gameRunning = running; }

    public ArrayList<Integer> getPlayerOrder() {
        return playerOrder;
    }


    public void addPlayerToOrder(Integer playerIdx) {
        playerOrder.add(playerIdx);
        Collections.shuffle(playerOrder);
    }


    public void removePlayerFromOrder(Integer playerIdx) {
        Integer turn = playerOrder.indexOf(playerIdx);
        playerOrder.remove(turn);
        Collections.shuffle(playerOrder);
    }


    public void setNumPlayers(int np) {
        numPlayers = np;
    }


    public int getNumPlayers() {
        return numPlayers;
    }

    public String getTextBoard() { // convert board to Text String
        int N = Board.N;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                sb.append(board.getSlotString(i, j));
                if (j != N-1) {
                    sb.append(",");
                }
            }
            sb.append("%");
        }
        return sb.toString();
    }



    public String makeMoveWord(int pID, int r, int c, char direction, String letters) {
        char[] alphabet = "abcdefghijklmno".toUpperCase().toCharArray();
        System.out.println(pID + " " + r + " " + c + " " + direction + " " + letters);
        Player player = players.get(pID);
        Hand hand = player.getHand();
        Word word = new Word(r, c, direction, letters);

        System.out.println("valid word?");
//        System.out.println(wordChecker.isValidWord(letters) != null);
        //wordChecker.isValidWord(letters) != null
        boolean isDirectionCorrect = word.getDirection() == 'H' || word.getDirection() == 'V';
        if (!isDirectionCorrect) {
            return "Invalid direction character: " + direction;
        }

        try {
            String err =  board.isValidPlacement(word, hand); // check if indexes and placement are valid then

            if ( err == null) { // SUCCESSFUL MOVE
                board.placeWord(word, hand);
                System.out.println("placed");
                turnMove = "WORD" + Protocol.UNIT_SEPARATOR + alphabet[c] + r + Protocol.UNIT_SEPARATOR + letters;

                curr_words.clear();
                turnScore  = calculateScore(word, board);
                player.incrementScore(turnScore );
                System.out.println("scored");

                System.out.println("Words Created: " + curr_words.toString());
                System.out.println("Score This Turn: " + turnScore );

                drawnTiles = hand.refill();
                System.out.println("refilled");
                System.out.println(drawnTiles);

                System.out.println("Bag: " + bag.size());
            }
            else {
                return err;
            }
        } catch (Exception e) {
            return e.getMessage();
        }

        return null;
    }


    private boolean areFormedWordsCorrect() {
        for (String word : curr_words) {
            if (wordChecker.isValidWord(word) != null) {
                return false;
            }
        }
        return true;
    }


    public String makeMoveSwap(int pID, String letters) {
        try {
            Hand hand = players.get(pID).getHand();
            String newLetters = hand.swap(letters);
            System.out.println("New letters: " + newLetters);
            System.out.println("Bag: " + bag.size());
            turnMove = "SWAP" + Protocol.UNIT_SEPARATOR + letters.length(); // tile count swapped
        }
        catch (Exception e) {
            return e.getMessage();
        }
        return  null;
    }

    public void reset() {
        playerOrder = new ArrayList<Integer>();
        players = new HashMap<>();
        wordChecker = new FileStreamScrabbleWordChecker();
        numPlayers = 0;
    }

    public boolean isGameOver() {
        if (bag.isEmpty()) { // bag is empty
            gameRunning = false; // game no longer running
            return true;
        }

        // one of the players ran out of tiles
        for (Integer pID : playerOrder) {
            Player p = players.get(pID);
            if (p == null) {
                removePlayerFromOrder(pID);
            }

            if (p.getHand().isEmpty()) {
                gameRunning = false;
                return true;
            }
        }

        return false;
    }


    public static int calculateScore(Word word, Board board) {
        int bingoScore = 0;
        int score = 0;
        int wordMultiplier = 1;
        int letterMultiplier = 1;
        int r = word.getRow();
        int c = word.getCol();
        curr_words.add(word.getLetters());

        for (int i = 0; i < word.getLength(); i++) {
            Slot Slot = board.getBoard()[r][c];

            switch (Slot.getPremiumType()) {
                case "C":
                case "2L":
                    letterMultiplier = 2;
                    break;
                case "2W":
                    wordMultiplier = 2;
                    break;
                case "3W":
                    wordMultiplier = 3;
                    break;
                case "3L":
                    letterMultiplier = 3;
                    break;
            }
            score += Slot.getTile().getScore() * letterMultiplier;

            if (word.getDirection() == 'H') c++;
            else r++;
        }
        score *= wordMultiplier;


        if (word.getLength() == Hand.LIMIT) {  // bingo! (if letters used in word are 7)
            score += 50;
        }

        return  score;
    }


}