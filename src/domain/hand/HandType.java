package domain.hand;

public enum HandType {
	HIGH_CARD(5, 1, 10, 1),
	PAIR(10, 2, 15, 1),
	TWO_PAIR(20, 2, 20, 1),
	THREE_OF_A_KIND(30, 3, 20, 2),
	STRAIGHT(30, 4, 30, 3),
	FLUSH(35, 4, 15, 2),
	FULL_HOUSE(40, 4, 25, 2),
	FOUR_OF_A_KIND(60, 7, 30, 3),
	STRAIGHT_FLUSH(100, 8, 40, 4),
	ROYAL_FLUSH(100, 8, 40, 4),
	FIVE_OF_A_KIND(120, 12, 35, 3),
	FLUSH_HOUSE(140, 14, 40, 4),
	FLUSH_FIVE(160, 16, 50, 3);

	private final int baseChips;
	private final int baseMult;
	private final int levelChips;
	private final int levelMult;

	HandType(int baseChips, int baseMult, int levelChips, int levelMult) {
		this.baseChips = baseChips;
		this.baseMult = baseMult;
		this.levelChips = levelChips;
		this.levelMult = levelMult;
	}

	public int baseChips() {
		return baseChips;
	}

	public int baseMult() {
		return baseMult;
	}

	public int levelChips() {
		return levelChips;
	}

	public int levelMult() {
		return levelMult;
	}
}
