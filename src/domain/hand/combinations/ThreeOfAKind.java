package domain.hand.combinations;

import domain.Card;
import domain.Rank;
import domain.consummables.Planet;
import domain.hand.HandType;

import java.util.List;
import java.util.Objects;

public record ThreeOfAKind(Rank rank, List<Card> scoringCards) implements PlayedHand {
	
	public ThreeOfAKind {
		Objects.requireNonNull(rank);
		Objects.requireNonNull(scoringCards);
	}
	
	@Override
	public HandType type() {
		return HandType.THREE_OF_A_KIND;
	}

	@Override
	public Planet planet() {
		return Planet.Venus;
	}
}
