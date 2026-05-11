package domain.hand.combinations;

import domain.Card;
import domain.Rank;
import domain.hand.HandType;

import java.util.List;

public record TwoPair(Rank high, Rank low, List<Card> scoringCards) implements PlayedHand {
	@Override public HandType type() { return HandType.TWO_PAIR; }
}
