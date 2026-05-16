package domain.consummables;

import domain.hand.HandType;

public enum Planet {
	Pluto(HandType.HIGH_CARD),
	Mercury(HandType.PAIR),
	Uranus(HandType.TWO_PAIR),
	Venus(HandType.THREE_OF_A_KIND),
	Saturn(HandType.STRAIGHT),
	Jupiter(HandType.FLUSH),
	Earth(HandType.FULL_HOUSE),
	Mars(HandType.FOUR_OF_A_KIND),
	Neptune(HandType.STRAIGHT_FLUSH);
	// PlanetX(HandType.FIVE_OF_A_KIND),
	// Ceres(HandType.FLUSH_HOUSE),
	// Eris(HandType.FLUSH_FIVE);

	private final HandType target;

	Planet(HandType target) {
		this.target = target;
	}

	public HandType getTarget() {
		return target;
	}
}
