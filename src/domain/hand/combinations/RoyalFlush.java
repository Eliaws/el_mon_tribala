package domain.hand.combinations;

import domain.Card;
import domain.Suit;
import domain.consummables.Planet;
import domain.hand.HandType;

import java.util.List;
import java.util.Objects;

public record RoyalFlush(Suit suit, List<Card> scoringCards) implements PlayedHand {
	
	public RoyalFlush {
		Objects.requireNonNull(suit);
		Objects.requireNonNull(scoringCards);
	}
	
	@Override
	public HandType type() {
		return HandType.ROYAL_FLUSH;
	}

	@Override
	public Planet planet() {
		return Planet.Neptune;
	}
}
