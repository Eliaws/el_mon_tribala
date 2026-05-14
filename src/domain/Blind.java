package domain;

public record Blind(int score) {
	public Blind {
		if(score < 1) {
			throw new IllegalArgumentException("Score must be > 1");
		}
	}
}
