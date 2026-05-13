package domain.hand.combinations;

import domain.Card;
import domain.Rank;
import domain.consummables.Planet;
import domain.hand.HandType;

import java.util.List;
import java.util.Objects;

public record Straight(Rank high, List<Card> scoringCards) implements PlayedHand {
	
	public Straight {
		Objects.requireNonNull(high);
		Objects.requireNonNull(scoringCards);
	}
	
	@Override
	public HandType type() {
		return HandType.STRAIGHT;
	}

	@Override
	public Planet planet() {
		return Planet.Saturn;
	}
}
