package nl.saad.scrabble.server.model;


import nl.saad.scrabble.protocol.Protocol;

public class Player {
    private String name;
    private Hand hand;
    private int score;

    
    public Player(Hand hand) {
        name = null;
        setHand(hand);
        score = 0;
    }

    public String getName() { return name; }

    public int getScore() {
        return score;
    }

    public Hand getHand() { return hand; }
    
    public void setName(String name) throws Exception {
        if (name == null || name.trim().equals("")) {
            throw new Exception(Protocol.Error.E013.getDescription());
        }
        this.name = name.trim();
    }

    public void setHand(Hand hand) { this.hand = hand; }
    
    public void incrementScore(int value) throws Exception {
        if (value < 0) {
            throw new Exception(Protocol.Error.E003.getDescription());
        }
        score += value;
    }
    
    public void decrementScore(int value) throws Exception {
        if (value < 0) {
            throw new Exception(Protocol.Error.E003.getDescription());
        }
        score -= value;
    }

}