import domain.Deck;

public class Main {

	public void main() {	
		Deck d = new Deck();
		d.generateBaseDeck();
		IO.println(d.getDeck().size());
		IO.println(d.getHand(8));
		d.showDeck();
	}
}
