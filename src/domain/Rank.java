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
	Jack(10),
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

	@Override
	public String toString() {
		return switch (this) {
			case Two -> "2";
			case Three -> "3";
			case Four -> "4";
			case Five -> "5";
			case Six -> "6";
			case Seven -> "7";
			case Eight -> "8";
			case Nine -> "9";
			case Ten -> "10";
			case Jack -> "J";
			case Queen -> "Q";
			case King -> "K";
			case Ace -> "A";
		};
	}
}
