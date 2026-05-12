package domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Deck {
	private final List<Card> deck;
	private final List<Card> discard;

	public Deck() {
		deck = new ArrayList<Card>();
		discard = new ArrayList<Card>();
	}

	public List<Card> getDeck() {
		return List.copyOf(deck);
	}

	public void generateBaseDeck() {
		for (Suit suit : Suit.values()) {
			for (Rank rank : Rank.values()) {
				deck.add(new Card(rank, suit));
			}
		}
	}

	public void showDeck() {
		for (Suit suit : Suit.values()) {
			String cards = deck.stream().filter(c -> c.suit() == suit).map(c -> c.rank().toString())
					.collect(Collectors.joining(", "));
			System.out.println(suit + ": " + cards);
		}
	}

	public List<Card> getHand(int size) {
		List<Card> out = new ArrayList<Card>();
		Random rand = new Random();
		while (out.size() < size) {
			if (deck.size() < 1) {
				reshuffle();
			}
			Card draw = deck.remove(rand.nextInt(deck.size()));
			discard.add(draw);
			out.add(draw);
		}
		return out;
	}

	private void reshuffle() {
		while (discard.size() > 0) {
			deck.add(discard.remove(0));
		}
	}

}