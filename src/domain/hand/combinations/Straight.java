package domain.hand.combinations;

import domain.Card;
import domain.Rank;
import domain.hand.HandType;

import java.util.List;

public record Straight(Rank high, List<Card> scoringCards) implements PlayedHand {
	@Override public HandType type() { return HandType.STRAIGHT; }
}
