package domain.hand.combinations;

import domain.Card;
import domain.Rank;
import domain.hand.HandType;

import java.util.List;
import java.util.Objects;

public record Pair(Rank rank, List<Card> scoringCards) implements PlayedHand {

	public Pair {
		Objects.requireNonNull(rank);
		Objects.requireNonNull(scoringCards);
	}

	@Override
	public HandType type() {
		return HandType.PAIR;
	}
}
