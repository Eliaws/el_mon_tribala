package domain.hand.combinations;

import domain.Card;
import domain.hand.HandType;

import java.util.List;
import java.util.Objects;

public record HighCard(Card card) implements PlayedHand {
	
	public HighCard {
		Objects.requireNonNull(card);
	}
	
	@Override
	public HandType type() {
		return HandType.HIGH_CARD;
	}

	@Override
	public List<Card> scoringCards() {
		return List.of(card);
	}
}
