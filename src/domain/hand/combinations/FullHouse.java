package domain.hand.combinations;

import domain.Card;
import domain.Rank;
import domain.consummables.Planet;
import domain.hand.HandType;

import java.util.List;
import java.util.Objects;

public record FullHouse(Rank three, Rank pair, List<Card> scoringCards) implements PlayedHand {
	
	public FullHouse {
		Objects.requireNonNull(three);
		Objects.requireNonNull(pair);
		Objects.requireNonNull(scoringCards);
	}
	
	@Override
	public HandType type() {
		return HandType.FULL_HOUSE;
	}

	@Override
	public Planet planet() {
		return Planet.Earth;
	}
}
