package domain;

public enum Suit {
    Clovers("♣"),
    Hearts("♥"),
    Spades("♠"),
    Diamonds("♦");

    private final String symbol;

    Suit(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
