package domain.hand.scoring;

public record Score(int chips, int mult) {

	public Score {
		if(chips < 0) {
			throw new IllegalArgumentException("chips must be positive");
		}
		if(mult < 1) {
			throw new IllegalArgumentException("multiplier must be > 1");
		}
	}
	
	public int total() {
		return chips * mult;
	}
}
