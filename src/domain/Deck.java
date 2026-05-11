package domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Deck {
	private final List<Card> deck;
	
	public Deck() {
		deck = new ArrayList<Card>();
	}

	public List<Card> getDeck() {
		return List.copyOf(deck);
	}
	
	public void generateBaseDeck(){
		for(Suit suit : Suit.values()) {
			for(Rank rank : Rank.values()) {
				deck.add(new Card(rank, suit));
			}
		}
	}

	public List<Card> getHand(int size) {
		List<Card> out = new ArrayList<Card>();
		Random rand = new Random();
		while(out.size()<size) {
			out.add(deck.get(rand.nextInt(deck.size())));
		}
		return out;
	}
	
	
}