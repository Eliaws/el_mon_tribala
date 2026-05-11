package domain.hand.combinations;

import domain.Card;
import domain.Rank;
import domain.Suit;
import domain.hand.HandType;

import java.util.List;
import java.util.Objects;

public record StraightFlush(Suit suit, Rank high, List<Card> scoringCards) implements PlayedHand {
	
	public StraightFlush {
		Objects.requireNonNull(suit);
		Objects.requireNonNull(high);
		Objects.requireNonNull(scoringCards);
	}
	
	@Override
	public HandType type() {
		return HandType.STRAIGHT_FLUSH;
	}
}
