package domain.hand.combinations;

import domain.Card;
import domain.Rank;
import domain.hand.HandType;

import java.util.List;

public record FourOfAKind(Rank rank, List<Card> scoringCards) implements PlayedHand {
	@Override public HandType type() { return HandType.FOUR_OF_A_KIND; }
}
