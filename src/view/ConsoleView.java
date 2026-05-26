package view;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import controller.GameController;
import controller.PlayResult;
import domain.Card;
import domain.Shop;
import domain.consummables.Planet;
import domain.hand.HandType;
import model.GamePhase;
import model.GameState;

public final class ConsoleView implements View {

	private static final String ESC = "\u001B";
	private static final String RESET = ESC + "[0m";
	private static final String BOLD = ESC + "[1m";
	private static final String DIM = ESC + "[2m";
	private static final String RED = ESC + "[31m";
	private static final String GREEN = ESC + "[32m";
	private static final String YELLOW = ESC + "[33m";
	private static final String CYAN = ESC + "[36m";
	private static final String ORANGE = ESC + "[38;5;208m";
	private static final String BRIGHT_GREEN = ESC + "[92m";
	private static final String BG_YELLOW = ESC + "[43m";
	private static final String BLACK = ESC + "[30m";
	private static final String CLEAR_SCREEN = ESC + "[2J" + ESC + "[H";

	private static final String[] BLIND_LABELS = { "Small Blind", "Big Blind", "Boss Blind" };

	private final GameController controller;
	private final Scanner scanner;
	private boolean running;

	public ConsoleView(GameController controller) {
		this.controller = controller;
		this.scanner = new Scanner(System.in);
		this.running = true;
	}

	@Override
	public void run() {
		controller.start();
		while (running) {
			GameState state = controller.getGameState();
			render(state);
			switch (state.getPhase()) {
				case PLAYING_BLIND -> handlePlay(state);
				case SHOP -> handleShop(state);
				case GAME_OVER, VICTORY -> handleEnd();
			}
		}
	}

	// ─── Boucle CLI : traduction entrées texte → actions du controller ─────

	private void handlePlay(GameState state) {
		String input = getUserInput();
		if (input.isEmpty()) {
			return;
		}

		String[] tokens = input.replace(",", " ").split("\\s+");
		String first = tokens[0];

		if (first.equals("p") || first.equals("play")) {
			executePlay(tokens);
			return;
		}
		if (first.equals("d") || first.equals("discard")) {
			executeDiscard(tokens);
			return;
		}

		char c = input.charAt(0);
		if (Character.isDigit(c)) {
			toggleTokens(input);
		} else if (c == 'c') {
			controller.clearSelection();
		} else if (c == 'r') {
			controller.sortHandByRank();
		} else if (c == 's') {
			controller.sortHandBySuit();
		} else if (c == 'h' || c == '?') {
			renderHelp();
			getUserInput();
		} else if (c == 'q') {
			running = false;
		} else {
			renderInvalidInput(input);
		}
	}

	private void handleShop(GameState state) {
		String input = getUserInput();
		if (input.isEmpty()) {
			return;
		}
		char c = input.charAt(0);
		if (Character.isDigit(c)) {
			Integer n = parseInt(input);
			if (n == null) {
				renderInvalidInput(input);
				return;
			}
			if (!controller.buyPlanet(n - 1)) {
				int price = Shop.PLANET_PRICE;
				if (state.getDollars() < price) {
					renderInvalidInput("Pas assez de dollars (besoin $" + price + ")");
				} else {
					renderInvalidInput("Offre indisponible");
				}
			}
		} else if (c == 'e') {
			controller.exitShop();
		} else if (c == 'h' || c == '?') {
			renderHelp();
			getUserInput();
		} else if (c == 'q') {
			running = false;
		} else {
			renderInvalidInput(input);
		}
	}

	private void handleEnd() {
		String input = getUserInput();
		if (input.isEmpty()) {
			return;
		}
		char c = input.charAt(0);
		if (c == 'r') {
			controller.restart();
		} else if (c == 'h' || c == '?') {
			renderHelp();
			getUserInput();
		} else if (c == 'q') {
			running = false;
		} else {
			renderInvalidInput(input);
		}
	}

	/**
	 * "p 1 3 5" → remplace la sélection courante par 1, 3, 5 et joue.
	 * "p" seul → joue la sélection courante.
	 */
	private void executePlay(String[] tokens) {
		if (tokens.length > 1) {
			controller.clearSelection();
			if (!selectIndices(tokens, 1)) {
				return;
			}
		}
		if (controller.getGameState().getSelectedCards().isEmpty()) {
			renderInvalidInput("Aucune carte sélectionnée");
			return;
		}
		PlayResult result = controller.play();
		if (result != null) {
			renderPlayResult(controller.getGameState(), result);
			getUserInput();
		}
	}

	/**
	 * "d 2 4" → remplace la sélection par 2, 4 et jette. "d" seul → jette la sélection.
	 */
	private void executeDiscard(String[] tokens) {
		if (tokens.length > 1) {
			controller.clearSelection();
			if (!selectIndices(tokens, 1)) {
				return;
			}
		}
		GameState state = controller.getGameState();
		if (state.getSelectedCards().isEmpty()) {
			renderInvalidInput("Aucune carte sélectionnée");
			return;
		}
		if (!controller.canDiscard()) {
			renderInvalidInput("Plus de discards disponibles");
			return;
		}
		controller.discard();
	}

	/**
	 * Toggle "1 3 5" sans jouer ni jeter. Stop au premier token invalide.
	 */
	private void toggleTokens(String input) {
		String[] tokens = input.replace(",", " ").split("\\s+");
		for (String token : tokens) {
			if (token.isEmpty()) {
				continue;
			}
			Integer n = parseInt(token);
			if (n == null) {
				renderInvalidInput("Token ignoré: " + token);
				continue;
			}
			if (!controller.toggle(n - 1)) {
				int handSize = controller.getGameState().getCurrentHand().size();
				if (n < 1 || n > handSize) {
					renderInvalidInput("Index hors main (1-" + handSize + "): " + n);
				} else {
					renderInvalidInput("Sélection max atteinte (" + controller.getGameState().getMaxSelected() + ")");
				}
				return;
			}
		}
	}

	private boolean selectIndices(String[] tokens, int start) {
		for (int i = start; i < tokens.length; i++) {
			Integer n = parseInt(tokens[i]);
			if (n == null) {
				renderInvalidInput("Token ignoré: " + tokens[i]);
				continue;
			}
			if (!controller.toggle(n - 1)) {
				int handSize = controller.getGameState().getCurrentHand().size();
				if (n < 1 || n > handSize) {
					renderInvalidInput("Index hors main (1-" + handSize + "): " + n);
				} else {
					renderInvalidInput("Sélection max atteinte (" + controller.getGameState().getMaxSelected() + ")");
				}
				return false;
			}
		}
		return true;
	}

	private Integer parseInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	// ─── I/O console (lecture + affichage) ─────────────────────────────────

	@Override
	public void render(GameState state) {
		System.out.print(CLEAR_SCREEN);
		switch (state.getPhase()) {
			case PLAYING_BLIND -> renderPlaying(state);
			case SHOP -> renderShop(state);
			case GAME_OVER -> renderGameOver(state);
			case VICTORY -> renderVictory(state);
		}
	}

	private String getUserInput() {
		System.out.print("\n> ");
		if (!scanner.hasNextLine()) {
			return "q";
		}
		return scanner.nextLine().trim().toLowerCase();
	}

	@Override
	public void renderInvalidInput(GameState state, String message) {
		renderInvalidInput(message);
	}

	private void renderInvalidInput(String input) {
		System.out.println();
		System.out.println(YELLOW + input + RESET);
	}

	@Override
	public void renderHelp() {
		System.out.print(CLEAR_SCREEN);
		System.out.println(
				BOLD + CYAN + "═══ Aide ═══" + RESET + "\n\n"
				+ BOLD + "Comment on joue" + RESET + "\n\n"
				+ "  Le but : atteindre le " + BOLD + "score cible" + RESET + " de la blinde courante\n"
				+ "  avant d'épuiser tes " + BOLD + "4 mains" + RESET + ". Sinon → GAME OVER.\n\n"
				+ "  À chaque tour :\n"
				+ "    1. Sélectionne " + BOLD + "1 à 5 cartes" + RESET + " parmi les 8 en main (commande [1-8])\n"
				+ "    2. Tape " + BOLD + "P" + RESET + " pour jouer → les cartes sont évaluées comme une\n"
				+ "       main de poker (Pair, Flush, Straight…) et marquent du score.\n"
				+ "    3. Tape " + BOLD + "D" + RESET + " à la place pour jeter et re-piocher (max 3 par blinde).\n\n"
				+ "  Score d'une main jouée :\n"
				+ "    (chips de base du type + valeur des cartes scorantes) × mult de base\n"
				+ "    ex. Pair de K = (10 + 10+10) × 2 = 60\n\n"
				+ "  Une partie = " + BOLD + "8 antes × 3 blindes" + RESET + " (Small / Big / Boss).\n"
				+ "  Entre chaque blinde, " + BOLD + "shop" + RESET + " pour acheter des planètes ($"
				+ Shop.PLANET_PRICE + " chacune)\n"
				+ "  qui level-up " + BOLD + "permanent" + RESET + " un type de main (+chips +mult).\n\n"
				+ "  Gain $ fin de blinde :\n"
				+ "    reward blinde ($3 Small, $4 Big, $5 Boss)\n"
				+ "    + $1 par main restante\n"
				+ "    + intérêts ($1 par tranche de $5 possédés, cap $5)\n\n"
				+ BOLD + "Commandes en blinde" + RESET + "\n"
				+ "  [1-8]            toggle une carte (sélection / désélection)\n"
				+ "  [1 3 5]          toggle plusieurs cartes d'un coup\n"
				+ "  P (ou play)      jouer la sélection courante\n"
				+ "  P 1 3 5          remplacer la sélection par 1, 3, 5 puis jouer\n"
				+ "  D (ou discard)   jeter la sélection courante (re-pioche)\n"
				+ "  D 2 4            remplacer la sélection par 2, 4 puis jeter\n"
				+ "  C                vider la sélection\n"
				+ "  R                trier la main par rang (2 → A)\n"
				+ "  S                trier la main par suit (♣ ♥ ♠ ♦)\n"
				+ "  H ou ?           cette aide\n"
				+ "  Q                quitter\n\n"
				+ BOLD + "Commandes shop" + RESET + "\n"
				+ "  [1-N]      acheter l'offre N ($" + Shop.PLANET_PRICE + ")\n"
				+ "  E          quitter le shop (passe au round suivant)\n\n"
				+ BOLD + "Hiérarchie des mains (plus faible → plus fort)" + RESET + "\n"
				+ "  HIGH_CARD         carte isolée\n"
				+ "  PAIR              2 cartes de même rang\n"
				+ "  TWO_PAIR          2 paires\n"
				+ "  THREE_OF_A_KIND   3 cartes de même rang\n"
				+ "  STRAIGHT          5 rangs consécutifs (A-2-3-4-5 et 10-J-Q-K-A OK)\n"
				+ "  FLUSH             5 cartes de même couleur\n"
				+ "  FULL_HOUSE        brelan + paire\n"
				+ "  FOUR_OF_A_KIND    4 cartes de même rang\n"
				+ "  STRAIGHT_FLUSH    straight + flush\n"
				+ "  ROYAL_FLUSH       10-J-Q-K-A même couleur\n\n"
				+ DIM + "(appuyer Entrée pour revenir)" + RESET);
	}

	@Override
	public void renderPlayResult(GameState state, PlayResult result) {
		System.out.print(CLEAR_SCREEN);
		System.out.println(BOLD + CYAN + "═══ Main jouée ═══" + RESET);
		System.out.println();
		System.out.println("Type:           " + BOLD + formatHandType(result.hand().type()) + RESET);

		StringBuilder all = new StringBuilder("Cartes jouées: ");
		for (Card c : result.playedCards()) {
			all.append(formatCard(c, false)).append(" ");
		}
		System.out.println(all);

		StringBuilder scoring = new StringBuilder("Scorantes:     ");
		for (Card c : result.hand().scoringCards()) {
			scoring.append(formatCard(c, false)).append(" ");
		}
		System.out.println(scoring);
		System.out.println();
		System.out.println("Score:   " + BOLD + result.score().chips() + RESET + " chips × "
				+ BOLD + result.score().mult() + RESET + " mult = "
				+ BOLD + GREEN + result.score().total() + RESET);
		System.out.println();

		int target = state.getBlinds().get((state.getRound() - 1) % 3).score();
		int totalScore = state.getCurrentBlindScore();
		GamePhase phase = state.getPhase();
		String prompt;
		if (phase == GamePhase.SHOP || phase == GamePhase.VICTORY) {
			System.out.println(BOLD + GREEN + "★ BLIND BATTUE !  " + totalScore + " / " + target + "  ★" + RESET);
			prompt = "(Entrée pour " + (phase == GamePhase.VICTORY ? "voir la victoire" : "entrer dans le shop") + ")";
		} else if (phase == GamePhase.GAME_OVER) {
			System.out.println(BOLD + RED + "× ÉCHEC : " + totalScore + " / " + target
					+ "  (manqué de " + (target - totalScore) + ") ×" + RESET);
			prompt = "(Entrée pour continuer)";
		} else {
			int handsLeft = state.getMaxHands() - state.getCurrentHandsPlay();
			System.out.println("Cumul: " + BOLD + totalScore + RESET + " / " + target
					+ "    Mains restantes: " + handsLeft + "/" + state.getMaxHands());
			prompt = "(Entrée pour continuer)";
		}
		System.out.println();
		System.out.println(DIM + prompt + RESET);
	}

	// ─── Helpers de rendu privés ───────────────────────────────────────────

	private void renderPlaying(GameState state) {
		int blindIdx = controller.getCurrentBlindIndex();
		int target = controller.getCurrentBlind().score();

		System.out.println(BOLD + CYAN + "═══ Tribala CLI ═══" + RESET);
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
			StringBuilder sel = new StringBuilder(
					"Sélection (" + selected.size() + "/" + state.getMaxSelected() + "): ");
			for (Card c : selected) {
				sel.append(formatCard(c, false)).append(" ");
			}
			System.out.println(sel);
			controller.previewSelection().ifPresent(p ->
					System.out.println("  " + DIM + "→ " + RESET + BOLD + formatHandType(p.type()) + RESET));
		}

		System.out.println();
		System.out.println(DIM + "[1-" + hand.size()
				+ "] toggle (espaces pour plusieurs)  ·  (P)lay  ·  (D)iscard  ·  (C)lear  ·  sort (R)ank/(S)uit  ·  (H)elp  ·  (Q)uit"
				+ RESET);
	}

	private void renderShop(GameState state) {
		System.out.println(BOLD + CYAN + "═══ Shop ═══" + RESET);
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
				System.out.println("      " + BOLD + formatHandType(target) + RESET
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
		int blindIdx = controller.getCurrentBlindIndex();
		int target = controller.getCurrentBlind().score();

		System.out.println(BOLD + RED + "═══ GAME OVER ═══" + RESET);
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
		System.out.println(BOLD + YELLOW + "═══ VICTORY ═══" + RESET);
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
}
