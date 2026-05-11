package domain;

import java.util.ArrayList;
import java.util.List;

public class Deck {
	private final List<Card> deck;
	
	public Deck() {
		deck = new ArrayList<Card>();
	}

	private void generateBaseDeck(){
		for(Suit suit : Suit.values()) {
			for(Rank rank : Rank.values()) {
				deck.add(new Card(rank, suit));
			}
		}
	}

	public List<Card> getDeck() {
		return deck;
	}

	public void main(){
		generateBaseDeck();
		IO.println(deck);
	}
}



// Combinaisaon hand = (HighCard) hand;
// Combinaisaon hand = 