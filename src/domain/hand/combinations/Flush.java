package domain.hand.combinations;

import domain.Card;
import domain.Suit;
import domain.hand.HandType;

import java.util.List;

public record Flush(Suit suit, List<Card> scoringCards) implements PlayedHand {
	@Override
	public HandType type() {
		return HandType.FLUSH;
	}
}
