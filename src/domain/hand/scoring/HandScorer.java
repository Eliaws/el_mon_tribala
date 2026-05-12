package domain.hand.scoring;

import domain.hand.HandType;
import domain.hand.combinations.PlayedHand;

public class HandScorer {

	public Score score(PlayedHand hand) {
		HandType type = hand.type();
		int chips = type.baseChips() + hand.scoringCards().stream()
				.mapToInt(card -> card.rank().value())
				.sum();
		return new Score(chips, type.baseMult());
	}
}
