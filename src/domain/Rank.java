package domain;

public enum Rank{
	Two(2),
	Three(3),
	Four(4),
	Five(5),
	Six(6),
	Seven(7),
	Eight(8),
	Nine(9),
	Ten(10),
	Jockey(10),
	Queen(10),	
	King(10),
	Ace(11);
	
	private int value;

	Rank(int v) {
		this.value = v;
	}

	public int value() {
		return value;
	}
}
