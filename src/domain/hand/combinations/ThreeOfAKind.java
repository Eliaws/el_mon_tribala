package domain.hand.combinations;

import domain.Card;
import domain.Rank;
import domain.hand.HandType;

import java.util.List;

public record ThreeOfAKind(Rank rank, List<Card> scoringCards) implements PlayedHand {
	@Override
	public HandType type() {
		return HandType.THREE_OF_A_KIND;
	}
}
