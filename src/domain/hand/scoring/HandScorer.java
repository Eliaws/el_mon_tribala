package domain.hand.scoring;

import java.util.Map;
import domain.hand.HandType;
import domain.hand.combinations.PlayedHand;

public class HandScorer {

	public static Score score(PlayedHand hand, Map<HandType, Integer> handLevels) {
		HandType type = hand.type();
		int level = handLevels.getOrDefault(type, 0);
		int chips = type.baseChips() + type.levelChips() * level + hand.scoringCards().stream()
				.mapToInt(card -> card.rank().value())
				.sum();
		int mult = type.baseMult() + type.levelMult() * level;
		return new Score(chips, mult);
	}
}
