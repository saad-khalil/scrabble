package nl.saad.scrabble.server.controller;

import nl.saad.scrabble.protocol.Protocol;
import nl.saad.scrabble.server.model.*;

import java.util.*;

import static java.lang.Math.max;


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
    public static final Set<String> allWordsFound = new HashSet<>();
    public static final Set<String> currWordStrings = new HashSet<>();
    public static final ArrayList<Word> currWords = new ArrayList<>();
    public static final int[] lastFourTurnScores = new int[4];


    public GameController() {
        playerOrder = new ArrayList<Integer>();
        players = new HashMap<>();
        wordChecker = new InMemoryScrabbleWordChecker();
        for (int i = 0; i < 4; i++) {
            lastFourTurnScores[i] = -1;
        }
    }

    public void startGame() {
        bag = new Bag();
        board = new Board();
        clearCurrWords();
        allWordsFound.clear();
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

    public void nextTurn(boolean skipped) {
        lastFourTurnScores[turn % 4] = turnScore;
        System.out.println(lastFourTurnScores.toString());
        turn++;
        turnScore = 0;
        turnMove = "";
        clearCurrWords();
        if (!skipped) {
            board.setFirstMove(false);
        }
    }

    public void clearCurrWords() {
        currWords.clear();
        currWordStrings.clear();
    }


    public String getDrawnTiles() {
        return drawnTiles;
    }

    public String getTurnMove() {
        return turnMove;
    }

    public int getTurnScore() {
        return turnScore;
    }

    public Board getBoard() {
        return board;
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public void setGameRunning(boolean running) {
        gameRunning = running;
    }

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
                if (j != N - 1) {
                    sb.append(",");
                }
            }
            sb.append("%");
        }
        return sb.toString();
    }


    public String makeMoveWord(int pID, int r, int c, char direction, String letters) {
        char[] alphabet = "abcdefghijklmno".toUpperCase().toCharArray();

        Player player = players.get(pID);
        Hand hand = player.getHand();


        // CHECK IF REQUIRED TILES ARE IN HAND (OR HAVE BLANKS TO COMPENSATE)
        int count_blanks = 0;
        char[] currentLetters = hand.getLetters().toCharArray(); // temporary hand
        for (int i = 0; i < Hand.LIMIT; i++) {
            if (currentLetters[i] == '!') {
                count_blanks++;
            }
        }

        int ri = r;
        int ci = c;
        for (int i = 0; i < letters.length(); i++) {
            char letter = letters.charAt(i);

            if (board.isSlotEmpty(ri, ci)) {
                int idx = new String(currentLetters).indexOf(letter);
                if (idx == -1) { // not in hand
                    if (count_blanks > 0) { // check if hand has blanks
                        count_blanks--; // a blank will be used for this character
                    } else {
                        return Protocol.Error.E008.getDescription();
                    }
                } else {
                    currentLetters[idx] = ' ';
                }
            }

            if (direction == 'H') ci++; // traverse horizontally over cols
            else ri++; // over rows otherwise
        }


        Word word = new Word(r, c, direction, letters);

        // COMPLETE WORD BY ITERATING LEFT RIGHT (H), OR UP DOWN (V) NEIGHBORING SLOTS
        word = completeWord(word);
        System.out.println("WORD USED: " + word.toString());

        if (wordChecker.isValidWord(word.getLetters()) == null) {
            return Protocol.Error.E006.getDescription();
        }

        boolean isDirectionCorrect = word.getDirection() == 'H' || word.getDirection() == 'V';
        if (!isDirectionCorrect) {
            return "Invalid direction character: " + direction;
        }

        try {
            String err = board.isValidPlacement(word, hand); // check if indexes and placement are valid then
            if (err != null) {
                return  err;
            }

            if (!scanNeighborWords(word)) { // also adds them to list if no conflict
                return "Neighboring words conflict / become invalid. Cannot place.";
            }

            currWordStrings.add(word.getLetters());
            currWords.add(word);

            // SUCCESSFUL MOVE
            board.placeWord(word, hand);
            allWordsFound.add(word.getLetters()); // add to game dictionary unique

            turnMove = "WORD" + Protocol.UNIT_SEPARATOR + alphabet[word.getCol()] + word.getRow() + Protocol.UNIT_SEPARATOR + letters;


            turnScore = calculateScore(word);
            lastFourTurnScores[turn % 4] = turnScore;

            player.incrementScore(turnScore);

            // all unique words found this turn
            allWordsFound.addAll(currWordStrings);


            System.out.println("Unique Words Created: " + currWordStrings.toString());
            System.out.println("Score This Turn: " + turnScore);

            drawnTiles = hand.refill();

            System.out.println("Bag: " + bag.size());

        } catch (Exception e) {
            return e.getMessage();
        }

        return null;
    }

    public String makeMoveSwap(int pID, String letters) {
        try {
            Hand hand = players.get(pID).getHand();
            drawnTiles = hand.swap(letters);
            System.out.println("New letters: " + drawnTiles);
            System.out.println("Bag: " + bag.size());
            turnMove = "SWAP" + Protocol.UNIT_SEPARATOR + letters.length(); // tile count swapped
            lastFourTurnScores[turn % 4] = turnScore;
        } catch (Exception e) {
            return e.getMessage();
        }
        return null;
    }

    public void reset() {
        playerOrder = new ArrayList<Integer>();
        players = new HashMap<>();
        wordChecker = new FileStreamScrabbleWordChecker();
        numPlayers = 0;
    }

    public boolean isGameOver() {
        boolean nonZeroTurnFound = false;
        for (int i = 0; i < 4; i++) {
            System.out.println(lastFourTurnScores[i]);
             if (lastFourTurnScores[i] != 0) {
                 nonZeroTurnFound = true;
             }
        }
        if (!nonZeroTurnFound) { // all zero turns before
            gameRunning = false;
            return true;
        }

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
            else if (p.getHand().isEmpty()) {
                gameRunning = false;
                return true;
            }
        }

        return false;
    }


    public int calculateScore(Word word) {
        int score = 0;

        for (Word w : currWords) {
            int wordScore = calculateWordScore(w);
            score += wordScore;
            System.out.println("Scoring " + w + " - " + wordScore);
        }

        if (word.getLength() == Hand.LIMIT) {  // bingo! (if letters used in word are 7)
            score += 50;
        }

        return score;
    }


    public int calculateWordScore(Word word) {
        int score = 0;
        int wordMultiplier = 1;
        int letterMultiplier = 1;
        int r = word.getRow();
        int c = word.getCol();

        for (int i = 0; i < word.getLength(); i++) {
            Slot Slot = board.getSlot(r, c);

            switch (Slot.getPremiumType()) {
                case "C":
                case "2W":
                    wordMultiplier = max(2, wordMultiplier);
                    break;
                case "2L":
                    letterMultiplier = 2;
                    break;
                case "3W":
                    wordMultiplier = 3;
                    break;
                case "3L":
                    letterMultiplier = 3;
                    break;
                default:
                    letterMultiplier = 1;
            }
            score += Slot.getTile().getScore() * letterMultiplier;

            if (word.getDirection() == 'H') c++;
            else r++;
        }

        return score * wordMultiplier;
    }

    //  SCANS neighbors for valid words adding them to turn dictionary, returns false if invalid found
    private boolean scanNeighborWords(Word word) {
        int r = word.getRow();
        int c = word.getCol();
        String letters = word.getLetters();
        char oppDir = word.getDirection() == 'H' ? 'V' : 'H';

        for (int i = 0; i < word.getLength(); i++) {
            String newChar = String.valueOf(letters.charAt(i));
//            System.out.println(r + ' ' + c + ' ' + oppDir + ' ' + newChar);
            Word newWord = completeWord(new Word(r, c, oppDir, newChar)); // get single character, form word IN OPPOSITE direction
            String newLetters = newWord.getLetters();

            System.out.println(newWord);

            // 1. not a character, 2. is a valid word and 3. not inside curr word strings or 4. even other words found in the game
            if (newWord.getLength() != 1 && !currWordStrings.contains(newLetters) && !allWordsFound.contains(newLetters)) {
                if (wordChecker.isValidWord(newLetters) == null) { // invalid word found in neighbors
                    return false;
                }
                currWordStrings.add(newLetters);
                currWords.add(newWord);
            }

            if (word.getDirection() == 'H') c++;
            else r++;
        }

        return true;
    }




    private Word completeWord(Word primaryWord) {
        int WL = primaryWord.getLength();
        char[][] tempBoard = board.getCharBoard();
        String letters = primaryWord.getLetters();
        int r = primaryWord.getRow();
        int c = primaryWord.getCol();


        if (primaryWord.getDirection() == 'H') {
            for (int i = 0; i < WL; i++) { // add current chars to temporary board
                tempBoard[r][c + i] = letters.charAt(i);
            }

            int cStartActual = primaryWord.getCol();
            int cEndActual = cStartActual + WL - 1;
            int cStart = cStartActual;
            int cEnd = cEndActual;


            while (board.isSlotValidOccupied(r, cStart - 1)) { // any tile before?
                cStart--; // left
            }

            while (board.isSlotValidOccupied(r, cEnd + 1)) { // any tile after tail?
                cEnd++; // right
            }

            // cStart and cEnd may point to same indices
            if (cStart == cStartActual && cEnd == cEndActual) { // same word, no new primary word
                return primaryWord;
            } else {
                StringBuilder sb = new StringBuilder(); // form new letters
                for (int ci = cStart; ci <= cEnd; ci++) {
                    char letter = tempBoard[r][ci];
                    sb.append(letter);
                }
                String newLetters = sb.toString();
                //                System.out.println("New primary horizontal word: " + newPrimaryWord.toString());
                return new Word(r, cStart, primaryWord.getDirection(), newLetters);
            }
        } else {
            for (int i = 0; i < WL; i++) { // add current chars to temporary board
                tempBoard[r + i][c] = letters.charAt(i);
            }

            int rStartActual = primaryWord.getRow();
            int rEndActual = rStartActual + WL - 1;
            int rStart = rStartActual;
            int rEnd = rEndActual;

            while (board.isSlotValidOccupied(rStart - 1, c)) { // any tile before?
                rStart--; // up
            }

            while (board.isSlotValidOccupied(rEnd + 1, c)) { // any tile after tail?
                rEnd++; // down
            }

            // rStart and rEnd may point to same indices
            if (rStart == rStartActual && rEnd == rEndActual) { // same word, no new primary word
                return primaryWord;
            } else {
                StringBuilder sb = new StringBuilder(); // form new letters
                for (int ri = rStart; ri <= rEnd; ri++) {
                    char letter = tempBoard[ri][c];
                    sb.append(letter);
                }
                String newLetters = sb.toString();
                return new Word(rStart, c, primaryWord.getDirection(), newLetters);
            }
        }

    }
}