package domain.hand.combinations;

import domain.Card;
import domain.hand.HandType;

import java.util.List;

public sealed interface PlayedHand permits HighCard, Pair, TwoPair, ThreeOfAKind, Straight, Flush, FullHouse,
		FourOfAKind, StraightFlush, RoyalFlush {

	HandType type();

	List<Card> scoringCards(); // Uniquement les cartes qui font la combinaison, pas les cartes en +
}
