package domain.hand.combinations;

import domain.Card;
import domain.Rank;
import domain.hand.HandType;

import java.util.List;

public record FullHouse(Rank three, Rank pair, List<Card> scoringCards) implements PlayedHand {
	@Override
	public HandType type() {
		return HandType.FULL_HOUSE;
	}
}
