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
        turn = 0;
        for (Integer pID : playerOrder) {
            players.put(pID, new Player(new Hand(bag)));
        }

        board.setFirstMove(true);
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


    public void nextTurn() {
        turn++;
        board.setFirstMove(false);
    }


    public Board getBoard() { return board; }


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
        System.out.println(pID + " " + r + " " + c + " " + direction + " " + letters);
        Player player = players.get(pID);
        Hand hand = player.getHand();
        Word word = new Word(r, c, direction, letters);

        System.out.println("valid word?");
//        System.out.println(wordChecker.isValidWord(letters) != null);
        //wordChecker.isValidWord(letters) != null
        try {
            if (  // check word letters
              (word.getDirection() == 'H' || word.getDirection() == 'V') // check direction
              && board.isValidPlacement(word, hand)) {             // check if indexes and placement are valid then

                board.placeWord(word, hand);
                System.out.println("placed");

                curr_words.clear();
                int score = calculateScore(word, board);
                player.incrementScore(score);
                System.out.println("scored");

                System.out.println("Words Created: " + curr_words.toString());
                System.out.println("Score This Turn: " + score);

                drawnTiles = hand.refill();
                System.out.println("refilled");
                System.out.println(drawnTiles);

                System.out.println("Bag: " + bag.size());
            }
            else {
                return "Invalid move.";
            }
            System.out.println(board);

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
        }
        catch (Exception e) {
            return e.getMessage();
        }
        return  null;
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