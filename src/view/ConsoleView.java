package view;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import controller.GameController;
import domain.Card;
import domain.Shop;
import domain.consummables.Planet;
import domain.hand.HandType;
import domain.hand.combinations.PlayedHand;
import domain.hand.evaluation.HandEvaluator;
import domain.hand.scoring.HandScorer;
import domain.hand.scoring.Score;
import model.GamePhase;
import model.GameState;

public class ConsoleView {

	private static final String RESET = "\u001B[0m";
	private static final String BOLD = "\u001B[1m";
	private static final String DIM = "\u001B[2m";
	private static final String RED = "\u001B[31m";
	private static final String GREEN = "\u001B[32m";
	private static final String YELLOW = "\u001B[33m";
	private static final String ORANGE = "\u001B[38;5;208m";
	private static final String BRIGHT_GREEN = "\u001B[92m";
	private static final String CYAN = "\u001B[36m";
	private static final String BG_YELLOW = "\u001B[43m";
	private static final String BLACK = "\u001B[30m";
	private static final String CLEAR_SCREEN = "\u001B[2J\u001B[H";

	private static final String[] BLIND_LABELS = { "Small Blind", "Big Blind", "Boss Blind" };

	private GameController controller;
	private final Scanner scanner;
	private boolean running;
	private String flash;

	public ConsoleView(GameController controller) {
		this.controller = controller;
		this.scanner = new Scanner(System.in);
		this.running = true;
		this.flash = null;
	}

	public void run() {
		controller.draw();
		while (running) {
			GameState state = controller.getGameState();
			render(state);
			switch (state.getPhase()) {
				case PLAYING_BLIND -> handlePlay(state);
				case SHOP -> handleShop(state);
				case GAME_OVER, VICTORY -> handleEnd();
			}
		}
		System.out.println(DIM + "Bye. ^^" + RESET);
	}

	// ─── Rendering ─────────────────────────────────────────────────────────

	private void render(GameState state) {
		System.out.print(CLEAR_SCREEN);
		switch (state.getPhase()) {
			case PLAYING_BLIND -> renderPlaying(state);
			case SHOP -> renderShop(state);
			case GAME_OVER -> renderGameOver(state);
			case VICTORY -> renderVictory(state);
		}
		if (flash != null) {
			System.out.println();
			System.out.println(YELLOW + flash + RESET);
			flash = null;
		}
	}

	private void renderPlaying(GameState state) {
		int blindIdx = (state.getRound() - 1) % 3;
		int target = state.getBlinds().get(blindIdx).score();

		System.out.println(BOLD + CYAN + "--- Tribala CLI ---" + RESET);
		System.out.println("Ante " + state.getAnte() + " · Round " + state.getRound()
				+ " · " + BOLD + BLIND_LABELS[blindIdx] + RESET);
		System.out.println("Score: " + BOLD + state.getCurrentBlindScore() + RESET + " / " + target
				+ "    Hands: " + state.getCurrentHandsPlay() + "/" + state.getMaxHands()
				+ "    Discards: " + state.getCurrentDiscards() + "/" + state.getMaxDiscards()
				+ "    " + YELLOW + "$" + state.getDollars() + RESET);
		System.out.println();

		List<Card> hand = state.getCurrentHand();
		List<Card> selected = state.getSelectedCards();
		System.out.println(BOLD + "Hand:" + RESET);
		StringBuilder sb = new StringBuilder("  ");
		for (int i = 0; i < hand.size(); i++) {
			Card c = hand.get(i);
			boolean isSel = selected.contains(c);
			sb.append(DIM).append("[").append(i + 1).append("]").append(RESET)
					.append(" ").append(formatCard(c, isSel)).append("   ");
		}
		System.out.println(sb);
		System.out.println();

		if (selected.isEmpty()) {
			System.out.println(DIM + "Aucune carte sélectionnée." + RESET);
		} else {
			StringBuilder sel = new StringBuilder("Sélection (" + selected.size() + "/" + state.getMaxSelected() + "): ");
			for (Card c : selected) {
				sel.append(formatCard(c, false)).append(" ");
			}
			System.out.println(sel);
			PlayedHand preview = HandEvaluator.evaluate(selected);
			System.out.println("  " + DIM + "→ " + RESET + BOLD + preview.type().name() + RESET);
		}

		System.out.println();
		System.out.println(DIM + "[1-" + hand.size() + "] toggle (espaces pour plusieurs)  ·  (P)lay  ·  (D)iscard  ·  (C)lear  ·  sort (R)ank/(S)uit  ·  (H)elp  ·  (Q)uit" + RESET);
	}

	private void renderShop(GameState state) {
		System.out.println(BOLD + CYAN + "--- Shop ---" + RESET);
		System.out.println(YELLOW + "Dollars: $" + state.getDollars() + RESET);
		System.out.println();

		List<Planet> offers = state.getShop().getOffers();
		System.out.println(BOLD + "Offres:" + RESET);
		if (offers.isEmpty()) {
			System.out.println("  " + DIM + "(stock épuisé)" + RESET);
		} else {
			for (int i = 0; i < offers.size(); i++) {
				Planet p = offers.get(i);
				HandType target = p.getTarget();
				int level = state.getHandLevels().getOrDefault(target, 0);
				System.out.println("  " + DIM + "[" + (i + 1) + "]" + RESET
						+ " " + BOLD + p.name() + RESET
						+ DIM + "  ($" + Shop.PLANET_PRICE + ")" + RESET);
				System.out.println("      ↳  " + BOLD + formatHandType(target) + RESET
						+ DIM + "  lvl " + level + " → " + (level + 1) + RESET
						+ "    " + GREEN + "+" + target.levelChips() + " chips, +" + target.levelMult() + " mult" + RESET);
			}
		}

		System.out.println();
		if (!state.getHandLevels().isEmpty()) {
			StringBuilder levels = new StringBuilder(DIM + "Niveaux acquis: " + RESET);
			boolean first = true;
			for (Map.Entry<HandType, Integer> e : state.getHandLevels().entrySet()) {
				if (!first) {
					levels.append(", ");
				}
				levels.append(formatHandType(e.getKey())).append(" (").append(e.getValue()).append(")");
				first = false;
			}
			System.out.println(levels);
			System.out.println();
		}
		String range = offers.isEmpty() ? "-" : "1-" + offers.size();
		System.out.println(DIM + "[" + range + "] buy  ·  (E)xit shop  ·  (H)elp  ·  (Q)uit" + RESET);
	}

	private void renderGameOver(GameState state) {
		int blindIdx = (state.getRound() - 1) % 3;
		int target = state.getBlinds().get(blindIdx).score();

		System.out.println(BOLD + RED + "--- GAME OVER ---" + RESET);
		System.out.println("Vaincu par " + BOLD + BLIND_LABELS[blindIdx] + RESET
				+ " (Ante " + state.getAnte() + ")");
		System.out.println("Score final: " + state.getCurrentBlindScore() + " / " + target
				+ "  (manqué de " + (target - state.getCurrentBlindScore()) + ")");
		System.out.println();
		System.out.println(DIM + "Stats: " + state.getPlayedHandStats() + RESET);
		System.out.println();
		System.out.println(DIM + "(R)estart  ·  (H)elp  ·  (Q)uit" + RESET);
	}

	private void renderVictory(GameState state) {
		System.out.println(BOLD + YELLOW + "--- VICTORY ---" + RESET);
		System.out.println("Ante 8 vaincu — bravo !");
		System.out.println();
		System.out.println(DIM + "Stats: " + state.getPlayedHandStats() + RESET);
		System.out.println(YELLOW + "Dollars: $" + state.getDollars() + RESET);
		System.out.println();
		System.out.println(DIM + "(R)estart  ·  (H)elp  ·  (Q)uit" + RESET);
	}

	private String formatHandType(HandType type) {
		return switch (type) {
			case HIGH_CARD -> "High Card";
			case PAIR -> "Pair";
			case TWO_PAIR -> "Two Pair";
			case THREE_OF_A_KIND -> "Three of a Kind";
			case STRAIGHT -> "Straight";
			case FLUSH -> "Flush";
			case FULL_HOUSE -> "Full House";
			case FOUR_OF_A_KIND -> "Four of a Kind";
			case STRAIGHT_FLUSH -> "Straight Flush";
			case ROYAL_FLUSH -> "Royal Flush";
			case FIVE_OF_A_KIND -> "Five of a Kind";
			case FLUSH_HOUSE -> "Flush House";
			case FLUSH_FIVE -> "Flush Five";
		};
	}

	private String formatCard(Card c, boolean selected) {
		String color = switch (c.suit()) {
			case Hearts -> RED;
			case Diamonds -> ORANGE;
			case Clovers -> BRIGHT_GREEN;
			case Spades -> "";
		};
		if (selected) {
			return BG_YELLOW + BLACK + " " + c.toString() + " " + RESET;
		}
		return " " + color + c.toString() + RESET;
	}

	// ─── Input handling ────────────────────────────────────────────────────

	private void handlePlay(GameState state) {
		String input = prompt();
		if (input.isEmpty()) {
			return;
		}
		char c = input.charAt(0);
		if (Character.isDigit(c)) {
			toggleTokens(input, state);
		} else if (c == 'p') {
			if (state.getSelectedCards().isEmpty()) {
				flash = "Aucune carte sélectionnée.";
			} else {
				List<Card> playedCards = List.copyOf(state.getSelectedCards());
				PlayedHand played = HandEvaluator.evaluate(playedCards);
				Score score = HandScorer.score(played, state.getHandLevels());
				controller.play();
				showPlayResult(state, playedCards, played, score);
			}
		} else if (c == 'd') {
			if (state.getSelectedCards().isEmpty()) {
				flash = "Aucune carte sélectionnée.";
			} else if (!controller.canDiscard()) {
				flash = "Plus de discards disponibles.";
			} else {
				controller.discard();
			}
		} else if (c == 'c') {
			state.getSelectedCards().clear();
		} else if (c == 'r') {
			controller.sortHandByRank();
		} else if (c == 's') {
			controller.sortHandBySuit();
		} else if (c == 'h' || c == '?') {
			showHelp();
		} else if (c == 'q') {
			running = false;
		} else {
			flash = "Commande inconnue: " + input + " (tape H pour l'aide)";
		}
	}

	private void handleShop(GameState state) {
		String input = prompt();
		if (input.isEmpty()) {
			return;
		}
		char c = input.charAt(0);
		if (Character.isDigit(c)) {
			Integer n = parseInt(input);
			if (n != null) {
				boolean ok = controller.buyPlanet(n - 1);
				if (!ok) {
					int price = Shop.PLANET_PRICE;
					if (state.getDollars() < price) {
						flash = "Pas assez de dollars (besoin $" + price + ").";
					} else {
						flash = "Offre indisponible.";
					}
				}
			}
		} else if (c == 'e') {
			controller.exitShop();
		} else if (c == 'h' || c == '?') {
			showHelp();
		} else if (c == 'q') {
			running = false;
		} else {
			flash = "Commande inconnue: " + input + " (tape H pour l'aide)";
		}
	}

	private void handleEnd() {
		String input = prompt();
		if (input.isEmpty()) {
			return;
		}
		char c = input.charAt(0);
		if (c == 'r') {
			controller = new GameController();
			controller.draw();
		} else if (c == 'h' || c == '?') {
			showHelp();
		} else if (c == 'q') {
			running = false;
		}
	}

	private void toggleTokens(String input, GameState state) {
		String[] tokens = input.replace(",", " ").split("\\s+");
		for (String token : tokens) {
			if (token.isEmpty()) {
				continue;
			}
			Integer n = parseInt(token);
			if (n == null) {
				flash = "Token ignoré: " + token;
				continue;
			}
			List<Card> hand = state.getCurrentHand();
			int idx = n - 1;
			if (idx < 0 || idx >= hand.size()) {
				flash = "Index hors main (1-" + hand.size() + "): " + n;
				continue;
			}
			Card card = hand.get(idx);
			if (state.getSelectedCards().contains(card)) {
				controller.unselect(idx);
			} else {
				boolean ok = controller.select(idx);
				if (!ok) {
					flash = "Sélection max atteinte (" + state.getMaxSelected() + ").";
					return;
				}
			}
		}
	}

	private void showPlayResult(GameState state, List<Card> playedCards, PlayedHand played, Score score) {
		System.out.print(CLEAR_SCREEN);
		System.out.println(BOLD + CYAN + "--- Main jouée ---" + RESET);
		System.out.println();
		System.out.println("Type:           " + BOLD + played.type().name() + RESET);

		StringBuilder all = new StringBuilder("Cartes jouées: ");
		for (Card c : playedCards) {
			all.append(formatCard(c, false)).append(" ");
		}
		System.out.println(all);

		StringBuilder scoring = new StringBuilder("Scorantes:     ");
		for (Card c : played.scoringCards()) {
			scoring.append(formatCard(c, false)).append(" ");
		}
		System.out.println(scoring);
		System.out.println();
		System.out.println("Score:   " + BOLD + score.chips() + RESET + " chips × "
				+ BOLD + score.mult() + RESET + " mult = "
				+ BOLD + GREEN + score.total() + RESET);
		System.out.println();

		int blindIdx = (state.getRound() - 1) % 3;
		int target = state.getBlinds().get(blindIdx).score();
		int totalScore = state.getCurrentBlindScore();
		GamePhase phase = state.getPhase();

		String prompt = "(Entrée pour continuer)";
		if (phase == GamePhase.SHOP || phase == GamePhase.VICTORY) {
			System.out.println(BOLD + GREEN + "★ BLIND BATTUE !  " + totalScore + " / " + target + "  ★" + RESET);
			prompt = "(Entrée pour entrer dans le shop)";
		} else if (phase == GamePhase.GAME_OVER) {
			System.out.println(BOLD + RED + "× ÉCHEC : " + totalScore + " / " + target
					+ "  (manqué de " + (target - totalScore) + ") ×" + RESET);
		} else {
			int handsLeft = state.getMaxHands() - state.getCurrentHandsPlay();
			System.out.println("Cumul: " + BOLD + totalScore + RESET + " / " + target
					+ "    Mains restantes: " + handsLeft + "/" + state.getMaxHands());
		}

		System.out.println();
		System.out.println(DIM + prompt + RESET);
		if (scanner.hasNextLine()) {
			scanner.nextLine();
		}
	}

	private void showHelp() {
		System.out.print(CLEAR_SCREEN);
		System.out.println(BOLD + CYAN + "--- Aide --- " + RESET);
		System.out.println();
		System.out.println(BOLD + "Comment on joue" + RESET);
		System.out.println();
		System.out.println("  Le but : atteindre le " + BOLD + "score cible" + RESET + " de la blinde courante");
		System.out.println("  avant d'épuiser tes " + BOLD + "4 mains" + RESET + ". Sinon → GAME OVER.");
		System.out.println();
		System.out.println("  À chaque tour :");
		System.out.println("    1. Sélectionne " + BOLD + "1 à 5 cartes" + RESET + " parmi les 8 en main (commande [1-8])");
		System.out.println("    2. Tape " + BOLD + "P" + RESET + " pour jouer → les cartes sont évaluées comme une");
		System.out.println("       main de poker (Pair, Flush, Straight…) et marquent du score.");
		System.out.println("    3. Tape " + BOLD + "D" + RESET + " à la place pour jeter et re-piocher (max 3 par blinde).");
		System.out.println();
		System.out.println("  Score d'une main jouée :");
		System.out.println("    (chips de base du type + valeur des cartes scorantes) × mult de base");
		System.out.println("    ex. Pair de K = (10 + 10+10) × 2 = 60");
		System.out.println();
		System.out.println("  Une partie = " + BOLD + "8 antes × 3 blindes" + RESET + " (Small / Big / Boss).");
		System.out.println("  Entre chaque blinde, " + BOLD + "shop" + RESET + " pour acheter des planètes ($3 chacune)");
		System.out.println("  qui level-up " + BOLD + "permanent" + RESET + " un type de main (+chips +mult).");
		System.out.println();
		System.out.println("  Gain $ fin de blinde :");
		System.out.println("    reward blinde ($3 Small, $4 Big, $5 Boss)");
		System.out.println("    + $1 par main restante");
		System.out.println("    + intérêts ($1 par tranche de $5 possédés, cap $5)");
		System.out.println();
		System.out.println(BOLD + "Commandes en blinde" + RESET);
		System.out.println("  [1-8]      toggle une carte (sélection / désélection)");
		System.out.println("  [1 3 5]    toggle plusieurs cartes d'un coup");
		System.out.println("  P          jouer la sélection");
		System.out.println("  D          jeter la sélection (re-pioche)");
		System.out.println("  C          vider la sélection");
		System.out.println("  sort R          trier la main par rang (2 → A)");
		System.out.println("  sort S          trier la main par suit (♣ ♥ ♠ ♦)");
		System.out.println("  H ou ?     cette aide");
		System.out.println("  Q          quitter");
		System.out.println();
		System.out.println(BOLD + "Commandes shop" + RESET);
		System.out.println("  [1-N]      acheter l'offre N ($" + Shop.PLANET_PRICE + ")");
		System.out.println("  E          quitter le shop (passe au round suivant)");
		System.out.println();
		System.out.println(BOLD + "Hiérarchie des mains (plus faible → plus fort)" + RESET);
		System.out.println("  HIGH_CARD         carte isolée");
		System.out.println("  PAIR              2 cartes de même rang");
		System.out.println("  TWO_PAIR          2 paires");
		System.out.println("  THREE_OF_A_KIND   3 cartes de même rang");
		System.out.println("  STRAIGHT          5 rangs consécutifs (A-2-3-4-5 et 10-J-Q-K-A OK)");
		System.out.println("  FLUSH             5 cartes de même couleur");
		System.out.println("  FULL_HOUSE        brelan + paire");
		System.out.println("  FOUR_OF_A_KIND    4 cartes de même rang");
		System.out.println("  STRAIGHT_FLUSH    straight + flush");
		System.out.println("  ROYAL_FLUSH       10-J-Q-K-A même couleur");
		System.out.println();
		System.out.println(DIM + "(appuyer Entrée pour revenir)" + RESET);
		if (scanner.hasNextLine()) {
			scanner.nextLine();
		}
	}

	private String prompt() {
		System.out.print("\n> ");
		if (!scanner.hasNextLine()) {
			running = false;
			return "";
		}
		return scanner.nextLine().trim().toLowerCase();
	}

	private Integer parseInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
