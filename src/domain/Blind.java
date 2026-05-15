package domain;

public record Blind(int score, int reward) {
	public Blind {
		if(score < 1) {
			throw new IllegalArgumentException("Score must be > 1");
		}
		if(reward < 0) {
			throw new IllegalArgumentException("Reward must be >= 0");
		}
	}
}
