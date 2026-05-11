package domain.hand.combinations;

import domain.Card;
import domain.Rank;
import domain.hand.HandType;

import java.util.List;
import java.util.Objects;

public record TwoPair(Rank high, Rank low, List<Card> scoringCards) implements PlayedHand {
	
	public TwoPair {
		Objects.requireNonNull(high);
		Objects.requireNonNull(low);
		Objects.requireNonNull(scoringCards);
	}
	
	@Override
	public HandType type() {
		return HandType.TWO_PAIR;
	}
}
