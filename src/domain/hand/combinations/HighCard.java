package domain.hand.combinations;

import domain.Card;
import domain.hand.HandType;
import domain.Rank;
import domain.Suit;

import java.util.List;
import java.util.Objects;

public record HighCard(Card card) implements PlayedHand {
	
	public HighCard {
		Objects.requireNonNull(card);
	}

	public HighCard(Rank rank, Suit suit, List<Card> cards) {
		this(new Card(rank, suit));
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
