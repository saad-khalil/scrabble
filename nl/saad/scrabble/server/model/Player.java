package nl.saad.scrabble.server.model;


public class Player {
    private String name;
    private Hand hand;
    private int score;

    
    public Player(Hand hand) {
        name = "";
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
    
    public void increaseScore(int value) throws Exception {
        if (value < 0) {
            throw new Exception(Protocol.Error.E003.getDescription());
        }
        score += value;
    }
    
    public void decreaseScore(int value) throws Exception {
        if (value < 0) {
            throw new Exception(Protocol.Error.E003.getDescription());
        }
        score -= value;
    }

    


}