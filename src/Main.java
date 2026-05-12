import java.util.List;

import domain.Card;
import domain.Rank;
import domain.Suit;
import domain.hand.combinations.PlayedHand;
import domain.hand.evaluation.HandEvaluator;

public class Main {

	public static void main(String[] args) {
		HandEvaluator evaluator = new HandEvaluator();

		test(evaluator, "HIGH_CARD", List.of(
				new Card(Rank.Two, Suit.Hearts),
				new Card(Rank.Five, Suit.Spades),
				new Card(Rank.Seven, Suit.Clovers),
				new Card(Rank.Ace, Suit.Diamonds),
				new Card(Rank.King, Suit.Hearts)));

		test(evaluator, "PAIR", List.of(
				new Card(Rank.Five, Suit.Hearts),
				new Card(Rank.Five, Suit.Spades),
				new Card(Rank.Seven, Suit.Clovers),
				new Card(Rank.Nine, Suit.Diamonds),
				new Card(Rank.King, Suit.Hearts)));

		test(evaluator, "TWO_PAIR", List.of(
				new Card(Rank.Five, Suit.Hearts),
				new Card(Rank.Five, Suit.Spades),
				new Card(Rank.Nine, Suit.Clovers),
				new Card(Rank.Nine, Suit.Diamonds),
				new Card(Rank.King, Suit.Hearts)));

		test(evaluator, "THREE_OF_A_KIND", List.of(
				new Card(Rank.Five, Suit.Hearts),
				new Card(Rank.Five, Suit.Spades),
				new Card(Rank.Five, Suit.Clovers),
				new Card(Rank.Nine, Suit.Diamonds),
				new Card(Rank.King, Suit.Hearts)));

		test(evaluator, "STRAIGHT (low)", List.of(
				new Card(Rank.Two, Suit.Hearts),
				new Card(Rank.Three, Suit.Spades),
				new Card(Rank.Four, Suit.Clovers),
				new Card(Rank.Five, Suit.Diamonds),
				new Card(Rank.Six, Suit.Hearts)));

		test(evaluator, "STRAIGHT (high)", List.of(
				new Card(Rank.Ten, Suit.Hearts),
				new Card(Rank.Jack, Suit.Spades),
				new Card(Rank.Queen, Suit.Clovers),
				new Card(Rank.King, Suit.Diamonds),
				new Card(Rank.Ace, Suit.Hearts)));

		test(evaluator, "STRAIGHT (wheel)", List.of(
				new Card(Rank.Ace, Suit.Hearts),
				new Card(Rank.Two, Suit.Spades),
				new Card(Rank.Three, Suit.Clovers),
				new Card(Rank.Four, Suit.Diamonds),
				new Card(Rank.Five, Suit.Hearts)));

		test(evaluator, "FLUSH", List.of(
				new Card(Rank.Two, Suit.Hearts),
				new Card(Rank.Five, Suit.Hearts),
				new Card(Rank.Seven, Suit.Hearts),
				new Card(Rank.Nine, Suit.Hearts),
				new Card(Rank.King, Suit.Hearts)));

		test(evaluator, "FULL_HOUSE", List.of(
				new Card(Rank.Five, Suit.Hearts),
				new Card(Rank.Five, Suit.Spades),
				new Card(Rank.Five, Suit.Clovers),
				new Card(Rank.Nine, Suit.Diamonds),
				new Card(Rank.Nine, Suit.Hearts)));

		test(evaluator, "FOUR_OF_A_KIND", List.of(
				new Card(Rank.Five, Suit.Hearts),
				new Card(Rank.Five, Suit.Spades),
				new Card(Rank.Five, Suit.Clovers),
				new Card(Rank.Five, Suit.Diamonds),
				new Card(Rank.King, Suit.Hearts)));

		test(evaluator, "STRAIGHT_FLUSH", List.of(
				new Card(Rank.Two, Suit.Hearts),
				new Card(Rank.Three, Suit.Hearts),
				new Card(Rank.Four, Suit.Hearts),
				new Card(Rank.Five, Suit.Hearts),
				new Card(Rank.Six, Suit.Hearts)));

		test(evaluator, "ROYAL_FLUSH", List.of(
				new Card(Rank.Ten, Suit.Hearts),
				new Card(Rank.Jack, Suit.Hearts),
				new Card(Rank.Queen, Suit.Hearts),
				new Card(Rank.King, Suit.Hearts),
				new Card(Rank.Ace, Suit.Hearts)));

		
	}

	private static void test(HandEvaluator evaluator, String expected, List<Card> cards) {
		String got;
		String scoring = "";
		try {
			PlayedHand hand = evaluator.evaluate(cards);
			got = hand.type().name();
			scoring = " | scoring=" + hand.scoringCards();
		} catch (Exception e) {
			got = "ERROR(" + e.getClass().getSimpleName() + ")";
		}
		String mark = got.equals(expected.split(" ")[0]) ? "OK  " : "FAIL";
		System.out.println(mark + " | expected " + expected + " | got " + got + scoring);
	}
}
