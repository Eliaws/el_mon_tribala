package domain;

import java.util.Objects;

public record Blind(String name, int score) {
	public Blind {
		Objects.requireNonNull(name);
		if(score < 1) {
			throw new IllegalArgumentException("Score must be > 1");
		}
	}
}
