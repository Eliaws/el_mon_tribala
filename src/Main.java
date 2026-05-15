import java.util.List;

import controller.GameController;
import domain.Card;
import domain.Rank;
import domain.Suit;
import domain.hand.combinations.PlayedHand;
import domain.hand.evaluation.HandEvaluator;
import model.GamePhase;
import model.GameState;

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

		scenarioFullCycle();
		scenarioDefeat();
		scenarioMultiBlind();
	}

	private static void scenarioFullCycle() {
		System.out.println("\n=== Scenario: blinde gagnee -> shop -> achat -> sortie ===\n");
		GameController controller = new GameController();
		GameState state = controller.getGameState();

		System.out.println("[Init] phase=" + state.getPhase()
				+ " | ante=" + state.getAnte()
				+ " | round=" + state.getRound()
				+ " | dollars=$" + state.getDollars()
				+ " | Small blind cible=" + state.getBlinds().get(0).score());

		state.getCurrentHand().clear();
		state.getCurrentHand().addAll(List.of(
				new Card(Rank.King, Suit.Hearts),
				new Card(Rank.Queen, Suit.Hearts),
				new Card(Rank.Jack, Suit.Hearts),
				new Card(Rank.Nine, Suit.Hearts),
				new Card(Rank.Five, Suit.Hearts)));
		System.out.println("[Hand injectee] " + state.getCurrentHand());

		for (int i = 0; i < 5; i++) {
			controller.select(i);
		}
		System.out.println("[Selectionne] " + state.getSelectedCards());

		controller.play();
		System.out.println("[Apres play] phase=" + state.getPhase()
				+ " | blind score=" + state.getCurrentBlindScore()
				+ " | dollars=$" + state.getDollars()
				+ " | stats=" + state.getPlayedHandStats());

		if (state.getPhase() == GamePhase.SHOP) {
			System.out.println("[Shop] offres=" + state.getShop().getOffers());

			boolean bought = controller.buyPlanet(0);
			System.out.println("[Achat offre 0] " + (bought ? "OK" : "FAIL")
					+ " | dollars=$" + state.getDollars()
					+ " | levels=" + state.getHandLevels());

			controller.exitShop();
			System.out.println("[Apres exitShop] phase=" + state.getPhase()
					+ " | ante=" + state.getAnte()
					+ " | round=" + state.getRound()
					+ " | hand size=" + state.getCurrentHand().size()
					+ " | nouveau Big blind cible=" + state.getBlinds().get(1).score());
		} else {
			System.out.println("[!] La blinde n'a pas ete gagnee, phase=" + state.getPhase());
		}
	}

	private static void scenarioDefeat() {
		System.out.println("\n=== Scenario: defaite apres 4 mains faibles ===\n");
		GameController controller = new GameController();
		GameState state = controller.getGameState();
		System.out.println("[Init] phase=" + state.getPhase()
				+ " | cible Small=" + state.getBlinds().get(0).score()
				+ " | maxHands=" + state.getMaxHands());

		for (int i = 1; i <= 4; i++) {
			state.getCurrentHand().clear();
			state.getCurrentHand().add(new Card(Rank.Two, Suit.Hearts));
			controller.select(0);
			controller.play();
			System.out.println("[Main " + i + "/4] phase=" + state.getPhase()
					+ " | score cumul=" + state.getCurrentBlindScore()
					+ " | hands joues=" + state.getCurrentHandsPlay());
		}
	}

	private static void scenarioMultiBlind() {
		System.out.println("\n=== Scenario: 3 blindes -> interest + transition ante ===\n");
		GameController controller = new GameController();
		GameState state = controller.getGameState();
		System.out.println("[Init] $" + state.getDollars()
				+ " | ante=" + state.getAnte()
				+ " | round=" + state.getRound()
				+ " | cible Small=" + state.getBlinds().get(0).score());

		String[] labels = { "Small", "Big", "Boss" };
		int[] rewards = { 3, 4, 5 };
		for (int i = 0; i < 3; i++) {
			int dollarsBefore = state.getDollars();
			int interest = Math.min(dollarsBefore / 5, 5);
			injectFlushAndPlay(controller, state);
			int handBonus = state.getMaxHands() - state.getCurrentHandsPlay();
			System.out.println("[Win " + labels[i] + "] score=" + state.getCurrentBlindScore()
					+ " | gain = $" + rewards[i] + "(reward) + $" + handBonus + "(mains restantes) + $" + interest
					+ "(interest) -> $" + state.getDollars());
			controller.exitShop();
			int idx = (state.getRound() - 1) % 3;
			System.out.println("[Exit shop] round=" + state.getRound()
					+ " | ante=" + state.getAnte()
					+ " | prochaine cible " + labels[idx] + "=" + state.getBlinds().get(idx).score());
		}
	}

	private static void injectFlushAndPlay(GameController controller, GameState state) {
		state.getCurrentHand().clear();
		state.getCurrentHand().addAll(List.of(
				new Card(Rank.King, Suit.Hearts),
				new Card(Rank.Queen, Suit.Hearts),
				new Card(Rank.Jack, Suit.Hearts),
				new Card(Rank.Nine, Suit.Hearts),
				new Card(Rank.Five, Suit.Hearts)));
		for (int i = 0; i < 5; i++) {
			controller.select(i);
		}
		controller.play();
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
