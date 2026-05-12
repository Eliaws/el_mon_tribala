package domain.hand.combinations;

import domain.Card;
import domain.Suit;
import domain.hand.HandType;

import java.util.List;
import java.util.Objects;

public record Flush(Suit suit, List<Card> scoringCards) implements PlayedHand {
	
	public Flush {
		Objects.requireNonNull(suit);
		Objects.requireNonNull(scoringCards);
	}
	
	@Override
	public HandType type() {
		return HandType.FLUSH;
	}
}
