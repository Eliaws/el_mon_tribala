package domain.hand.scoring;

import java.util.Map;

import domain.consummables.Planet;
import domain.hand.HandType;
import domain.hand.combinations.PlayedHand;

public class HandScorer {

	public Score score(PlayedHand hand, Map<Planet, Integer> planets) {
		HandType type = hand.type();
		int planetChips = hand.planet().getChips() * planets.getOrDefault(hand.planet(), 0);
		int planetMult = hand.planet().getMult() * planets.getOrDefault(hand.planet(), 0);
		int chips = planetChips + type.baseChips() + hand.scoringCards().stream()
				.mapToInt(card -> card.rank().value())
				.sum();
		int mult = type.baseMult() + planetMult;
		return new Score(chips, mult);
	}
}
