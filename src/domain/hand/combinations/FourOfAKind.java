package domain.hand.combinations;

import domain.Card;
import domain.Rank;
import domain.hand.HandType;

import java.util.List;
import java.util.Objects;

public record FourOfAKind(Rank rank, List<Card> scoringCards) implements PlayedHand {

	public FourOfAKind {
		Objects.requireNonNull(rank);
		Objects.requireNonNull(scoringCards);
	}

	@Override
	public HandType type() {
		return HandType.FOUR_OF_A_KIND;
	}
}
