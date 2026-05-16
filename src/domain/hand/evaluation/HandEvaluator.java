package domain.hand.evaluation;

import domain.Card;
import domain.Rank;
import domain.Suit;
import domain.hand.combinations.Flush;
import domain.hand.combinations.FourOfAKind;
import domain.hand.combinations.FullHouse;
import domain.hand.combinations.HighCard;
import domain.hand.combinations.Pair;
import domain.hand.combinations.PlayedHand;
import domain.hand.combinations.RoyalFlush;
import domain.hand.combinations.Straight;
import domain.hand.combinations.StraightFlush;
import domain.hand.combinations.ThreeOfAKind;
import domain.hand.combinations.TwoPair;

import java.util.List;
import java.util.stream.Collectors;

public class HandEvaluator {

	public static PlayedHand evaluate(List<Card> cards) {
		if(isRoyalFlush(cards)) {
			var suit = cards.get(0).suit();
			return new RoyalFlush(suit, cards);
			
		} else if(isStraightFlush(cards)) {
			var high = straightHigh(cards);
			var suit = cards.get(0).suit();
			return new StraightFlush(suit, high, cards);

		} else if(isFourOfAKind(cards)) {
			var rank = cards.stream().filter(c -> cards.stream().filter(cc -> cc.rank() == c.rank()).count() == 4).findFirst().get().rank();
			var scoring = cards.stream().filter(c -> c.rank() == rank).toList();
			return new FourOfAKind(rank, scoring);

		} else if(isFullHouse(cards)) {
			var three = cards.stream().filter(c -> cards.stream().filter(cc -> cc.rank() == c.rank()).count() == 3).findFirst().get().rank();
			var pair = cards.stream().filter(c -> cards.stream().filter(cc -> cc.rank() == c.rank()).count() == 2).findFirst().get().rank();
			return new FullHouse(three, pair, cards);

		} else if(isFlush(cards)) {
			var suit = cards.get(0).suit();
			return new Flush(suit, cards);

		} else if(isStraight(cards)) {
			var high = straightHigh(cards);
			return new Straight(high, cards);

		} else if(isThreeOfAKind(cards)) {
			var rank = cards.stream().filter(c -> cards.stream().filter(cc -> cc.rank() == c.rank()).count() == 3).findFirst().get().rank();
			var scoring = cards.stream().filter(c -> c.rank() == rank).toList();
			return new ThreeOfAKind(rank, scoring);

		} else if(isTwoPair(cards)) {
			var pair1 = cards.stream().filter(c -> cards.stream().filter(cc -> cc.rank() == c.rank()).count() == 2).findFirst().get().rank();
			var pair2 = cards.stream().filter(c -> cards.stream().filter(cc -> cc.rank() == c.rank()).count() == 2 && c.rank() != pair1).findFirst().get().rank();
			var scoring = cards.stream().filter(c -> c.rank() == pair1 || c.rank() == pair2).toList();
			return new TwoPair(pair1, pair2, scoring);

		} else if(isPair(cards)) {
			var pair = cards.stream().filter(c -> cards.stream().filter(cc -> cc.rank() == c.rank()).count() == 2).findFirst().get().rank();
			var scoring = cards.stream().filter(c -> c.rank() == pair).toList();
			return new Pair(pair, scoring);

		} else {
			return highCard(cards);
		}
	}

	public static boolean isRoyalFlush(List<Card> cards) {
		if(isStraightFlush(cards)) {
			if(cards.stream().anyMatch(c -> c.rank() == Rank.Ten) &&
			   cards.stream().anyMatch(c -> c.rank() == Rank.Jack) &&
			   cards.stream().anyMatch(c -> c.rank() == Rank.Queen) &&
			   cards.stream().anyMatch(c -> c.rank() == Rank.King) &&
			   cards.stream().anyMatch(c -> c.rank() == Rank.Ace)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isStraightFlush(List<Card> cards) {
		if(isFlush(cards) && isStraight(cards)) {
			return true;
		}
		return false;
	}

	public static boolean isFourOfAKind(List<Card> cards) {
		if(cards.stream().anyMatch(c -> cards.stream().filter(cc -> cc.rank() == c.rank()).count() == 4)) {
			return true;
		}
		return false;

	}

	public static boolean isFullHouse(List<Card> cards) {
		boolean hasThreeOfAKind = cards.stream().anyMatch(c -> cards.stream().filter(cc -> cc.rank() == c.rank()).count() == 3);
		boolean hasPair = cards.stream().anyMatch(c -> cards.stream().filter(cc -> cc.rank() == c.rank()).count() == 2);
		return hasThreeOfAKind && hasPair;
	}

	public static boolean isFlush(List<Card> cards) {
		if (cards.size() != 5) {
			return false;
		}
		Suit suit = cards.get(0).suit();
		return cards.stream().allMatch(c -> c.suit() == suit);
	}

	public static boolean isStraight(List<Card> cards) {
		if (cards.size() != 5) {
			return false;
		}
		List<Rank> ranks = cards.stream().map(Card::rank).sorted().collect(Collectors.toList());
		// Test pour : A-2-3-4-5
		if (ranks.get(0) == Rank.Two && ranks.get(1) == Rank.Three && ranks.get(2) == Rank.Four
				&& ranks.get(3) == Rank.Five && ranks.get(4) == Rank.Ace) {
			return true;
		}
		for(int i = 1; i < ranks.size(); i++) {
			if(ranks.get(i).ordinal() != ranks.get(i-1).ordinal() + 1) {
				return false;
			}
		}
		return true;
	}

	private static Rank straightHigh(List<Card> cards) {
		List<Rank> ranks = cards.stream().map(Card::rank).sorted().collect(Collectors.toList());
		if (ranks.get(0) == Rank.Two && ranks.get(4) == Rank.Ace) {
			return Rank.Five;
		}
		return ranks.get(4);
	}

	public static boolean isThreeOfAKind(List<Card> cards) {
		return cards.stream().anyMatch(c -> cards.stream().filter(cc -> cc.rank() == c.rank()).count() == 3);
	}

	public static boolean isTwoPair(List<Card> cards) {
		long pairCount = cards.stream().collect(Collectors.groupingBy(Card::rank))
				.values().stream().filter(g -> g.size() == 2).count();
		return pairCount == 2;
	}

	public static boolean isPair(List<Card> cards) {
		return cards.stream().anyMatch(c -> cards.stream().filter(cc -> cc.rank() == c.rank()).count() == 2);
	}

	public static HighCard highCard(List<Card> cards) {
		Card highest = cards.stream().max(java.util.Comparator.comparing(Card::rank)).get();
		return new HighCard(highest);
	}
}
