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
    public static final Set<String> all_words_found = new HashSet<String>();
    public static final Set<String> currWords = new HashSet<String>();


    public GameController() {
        playerOrder = new ArrayList<Integer>();
        players = new HashMap<>();
        wordChecker = new InMemoryScrabbleWordChecker();
    }

    public void startGame() {
        bag = new Bag();
        board = new Board();
        currWords.clear();
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
        turn++;
        turnScore = 0;
        turnMove = "";
        if (!skipped) {
            board.setFirstMove(false);
        }
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



        // CHECK IF REQUIRED TILES ARE IN HAND (OR HAVE BLANKS TO COMPENSATE)
        int LIMIT = 7;
        int count_blanks = 0;
        char[] currentLetters = hand.getLetters().toCharArray(); // temporary hand
        for (int i = 0; i < LIMIT; i++) {
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
                    }
                    else {
                        return Protocol.Error.E008.getDescription();
                    }
                }
                else {
                    currentLetters[idx] = ' ';
                }
            }

            if (direction == 'H') ci++; // traverse horizontally over cols
            else ri++; // over rows otherwise
        }



        Word word = new Word(r, c, direction, letters);

        // COMPLETE WORD BY ITERATING LEFT RIGHT (H), OR UP DOWN (V) NEIGHBORING SLOTS
        word = completePrimaryWord(word);
        System.out.println("WORD USED: " + word.toString());

        if (wordChecker.isValidWord(word.getLetters()) == null) {
            return Protocol.Error.E006.getDescription();
        }

        boolean isDirectionCorrect = word.getDirection() == 'H' || word.getDirection() == 'V';
        if (!isDirectionCorrect) {
            return "Invalid direction character: " + direction;
        }

        try {
            String err =  board.isValidPlacement(word, hand); // check if indexes and placement are valid then

            if ( err == null) { // SUCCESSFUL MOVE
                board.placeWord(word, hand);
                System.out.println("placed");
                turnMove = "WORD" + Protocol.UNIT_SEPARATOR + alphabet[word.getCol()] + word.getRow() + Protocol.UNIT_SEPARATOR + letters;

                currWords.clear();
                turnScore  = calculateScore(word, board);
                player.incrementScore(turnScore );

                System.out.println("Words Created: " + currWords.toString());
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
        for (String word : currWords) {
            if (wordChecker.isValidWord(word) != null) {
                return false;
            }
        }
        return true;
    }


    public String makeMoveSwap(int pID, String letters) {
        try {
            Hand hand = players.get(pID).getHand();
            drawnTiles = hand.swap(letters);
            System.out.println("New letters: " + drawnTiles);
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


    public int calculateScore(Word word, Board board) {
        int bingoScore = 0;
        int score = 0;
        int wordMultiplier = 1;
        int letterMultiplier = 1;
        int r = word.getRow();
        int c = word.getCol();
        currWords.add(word.getLetters());

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


        score += scoreExtraWords(word); // find and score words other than primary


        if (word.getLength() == Hand.LIMIT) {  // bingo! (if letters used in word are 7)
            score += 50;
        }

        return  score;
    }

    private Word completePrimaryWord(Word primaryWord) {
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


            while (board.isSlotValidOccupied(r, cStart-1)) { // any tile before?
                cStart--; // left
            }

            while (board.isSlotValidOccupied(r, cEnd+1)) { // any tile after tail?
                cEnd++; // right
            }

            // cStart and cEnd may point to same indices
            if (cStart == cStartActual && cEnd == cEndActual) { // same word, no new primary word
                return primaryWord;
            }
            else {
                StringBuilder sb = new StringBuilder(); // form new letters
                for (int ci = cStart; ci <= cEnd; ci++) {
                    char letter = tempBoard[r][ci];
                    sb.append(letter);
                }
                String newLetters = sb.toString();
                Word newPrimaryWord = new Word(r, cStart, primaryWord.getDirection(), newLetters);
                System.out.println("New primary horizontal word: " + newPrimaryWord.toString());
                return  newPrimaryWord;
            }
        }
        else {
            for (int i = 0; i < WL; i++) { // add current chars to temporary board
                tempBoard[r+i][c] = letters.charAt(i);
            }

            int rStartActual = primaryWord.getRow();
            int rEndActual = rStartActual + WL - 1;
            int rStart = rStartActual;
            int rEnd = rEndActual;

            while (board.isSlotValidOccupied(rStart-1, c)) { // any tile before?
                rStart--; // up
            }

            while (board.isSlotValidOccupied(rEnd+1, c)) { // any tile after tail?
                rEnd++; // down
            }

            System.out.println(rStart);
            System.out.println(rStartActual);

            System.out.println(rEnd);
            System.out.println(rEndActual);

            // rStart and rEnd may point to same indices
            if (rStart == rStartActual && rEnd == rEndActual) { // same word, no new primary word
                return primaryWord;
            }
            else {
                StringBuilder sb = new StringBuilder(); // form new letters
                for (int ri = rStart; ri <= rEnd; ri++) {
                    char letter = tempBoard[ri][c];
                    sb.append(letter);
                }
                String newLetters = sb.toString();
                Word newPrimaryWord = new Word(rStart, c, primaryWord.getDirection(), newLetters);
                System.out.println("New primary vertical word: " + newPrimaryWord.toString());
                return  newPrimaryWord;
            }
        }
        
    }

    // Scores all the words hooked/parallel to a vertically placed word
    private int scoreExtraWords(Word primaryWord) {
        int score = 0;
        int wordScore = 0;
        int wordMultiplier = 1;
        Slot[][] b = board.getBoard();

//        if (primaryWord.getDirection() == 'H') {
//
//        }
//        else {
//
//        }
        //HORIZONTAL
        int WL = primaryWord.getLength();
        int r = primaryWord.getRow();

        int cStartActual = primaryWord.getCol();
        int cEndActual = primaryWord.getCol() + WL - 1;

        int cStart = cStartActual;
        int cEnd = cEndActual;

        while (board.isSlotValidOccupied(cStart-1, r)) { // any tile before?
            cStart--; // move left
        }

        while (board.isSlotValidOccupied(cEnd+1, r)) { // any tile after tail?
            cEnd++; // move right
        }

        // cStart and cEnd may point to same word

        // Add word score for any extra words formed
        if (cStart != cStartActual && cEnd != cEndActual) { // new word

        }

//        for (int i = startColumn; i <= endColumn; i++) {
//            Slot Slot = b[r][i];
//            if (i == index.getCol()) {
//                wordScore += Slot.getTile().getScore() * Slot.getLetterMultiplier();
//                wordMultiplier *= Slot.getWordMultiplier();
//            } else {
//                wordScore += Slot.getTile().getScore();
//            }
//        }
//        score += wordScore * wordMultiplier;
//        currWords.add(board.getHorizontalWord(r, startColumn, endColumn));

        return score;
    }


//    Slot[][] b = board.getBoard();
//        for (int i = 0; i < word.getLength(); i++) {
//        int wordScore = 0;
//        int wordMultiplier = 1;
//        int startRow = index.getRow();
//        int endRow = index.getRow();
//        int c = index.getCol();
//
//        if (word.getDirection() == 'H') {
//            c++;
//        } else {
//            r++;
//        }
//
//        // Find the starting r index of the word
//        while (Slot.isValid(c, startRow - 1) && !b[startRow - 1][c].isEmpty()) {
//            startRow--;
//        }
//        // Find the tail r index of the word
//        while (Slot.isValid(c, endRow + 1) && !b[endRow + 1][c].isEmpty()) {
//            endRow++;
//        }
//        // Add word score for any extra words formed
//        if (startRow != endRow) {
//            for (int i = startRow; i <= endRow; i++) {
//                Slot Slot = b[i][c];
//                if (i == index.getRow()) {
//                    wordScore += Slot.getTile().getScore() * Slot.getLetterMultiplier();
//                    wordMultiplier *= Slot.getWordMultiplier();
//                } else {
//                    wordScore += Slot.getTile().getScore();
//                }
//            }
//            score += wordScore * wordMultiplier;
//            currWords.add(board.getVerticalWord(c, startRow, endRow));
//        }
//    }

}