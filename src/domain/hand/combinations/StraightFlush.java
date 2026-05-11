package domain.hand.combinations;

import domain.Card;
import domain.Rank;
import domain.Suit;
import domain.hand.HandType;

import java.util.List;

public record StraightFlush(Suit suit, Rank high, List<Card> scoringCards) implements PlayedHand {
	@Override public HandType type() { return HandType.STRAIGHT_FLUSH; }
}
