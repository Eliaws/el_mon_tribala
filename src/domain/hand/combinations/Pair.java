package domain.hand.combinations;

import domain.Card;
import domain.Rank;
import domain.hand.HandType;

import java.util.List;

public record Pair(Rank rank, List<Card> scoringCards) implements PlayedHand {
	@Override
	public HandType type() {
		return HandType.PAIR;
	}
}
