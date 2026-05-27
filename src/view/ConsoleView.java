package view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import controller.PlayResult;
import domain.Card;
import domain.Shop;
import domain.consummables.Planet;
import domain.hand.HandType;
import domain.hand.combinations.PlayedHand;
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

	private final Scanner scanner;

	public ConsoleView() {
		this.scanner = new Scanner(System.in);
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
		case PLAYING_BLIND ->
			renderPlaying(state, (state.getRound() - 1) % 3, state.getBlinds().get((state.getRound() - 1) % 3).score(), state.getPreviewHand());
		case SHOP -> renderShop(state);
		case GAME_OVER -> renderGameOver(state, state.getRound(), state.getCurrentBlindScore());
		case VICTORY -> renderVictory(state);
		}
	}

	public List<String> getUserInput(GameState state) {
		System.out.print("\n> ");
		if (!scanner.hasNextLine()) {
			return List.of("q");
		}
		List<String> input = List.of(scanner.nextLine().trim().toLowerCase().replace(",", " ").split("\\s+"));

		if (input.get(0).equals("p") || input.get(0).equals("play") || input.get(0).equals("d")
				|| input.get(0).equals("discard") || input.get(0).equals("h") || input.get(0).equals("help")
				|| input.get(0).equals("s") || input.get(0).equals("r") || input.get(0).equals("q")
				|| input.get(0).equals("quit") || input.get(0).equals("c") || input.get(0).equals("?") || input.get(0).equals("e")) {
			return input;
		} else {
			try {
				ArrayList<String> out = new ArrayList<String>();
				for (int i = 0; i < input.size(); i++) {
					Integer n = parseInt(input.get(i));
					if (n == null) {
						continue;
					}
					if (state != null && (n > state.getHandSize() || n <= 0)) {
						return List.of("invalid", "OutOfBoundsException");
					}
					n -= 1;
					out.add(n.toString());
				}
				return out;
			} catch (NumberFormatException _) {
				return List.of("invalid", "invalidCommand");
			}
		}
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
		System.out.println(BOLD + CYAN + "═══ Aide ═══" + RESET + "\n\n" + BOLD + "Comment on joue" + RESET + "\n\n"
				+ "  Le but : atteindre le " + BOLD + "score cible" + RESET + " de la blinde courante\n"
				+ "  avant d'épuiser tes " + BOLD + "4 mains" + RESET + ". Sinon → GAME OVER.\n\n"
				+ "  À chaque tour :\n" + "    1. Sélectionne " + BOLD + "1 à 5 cartes" + RESET
				+ " parmi les 8 en main (commande [1-8])\n" + "    2. Tape " + BOLD + "P" + RESET
				+ " pour jouer → les cartes sont évaluées comme une\n"
				+ "       main de poker (Pair, Flush, Straight…) et marquent du score.\n" + "    3. Tape " + BOLD + "D"
				+ RESET + " à la place pour jeter et re-piocher (max 3 par blinde).\n\n"
				+ "  Score d'une main jouée :\n"
				+ "    (chips de base du type + valeur des cartes scorantes) × mult de base\n"
				+ "    ex. Pair de K = (10 + 10+10) × 2 = 60\n\n" + "  Une partie = " + BOLD + "8 antes × 3 blindes"
				+ RESET + " (Small / Big / Boss).\n" + "  Entre chaque blinde, " + BOLD + "shop" + RESET
				+ " pour acheter des planètes ($" + Shop.PLANET_PRICE + " chacune)\n" + "  qui level-up " + BOLD
				+ "permanent" + RESET + " un type de main (+chips +mult).\n\n" + "  Gain $ fin de blinde :\n"
				+ "    reward blinde ($3 Small, $4 Big, $5 Boss)\n" + "    + $1 par main restante\n"
				+ "    + intérêts ($1 par tranche de $5 possédés, cap $5)\n\n" + BOLD + "Commandes en blinde" + RESET
				+ "\n" + "  [1-8]            toggle une carte (sélection / désélection)\n"
				+ "  [1 3 5]          toggle plusieurs cartes d'un coup\n"
				+ "  P (ou play)      jouer la sélection courante\n"
				+ "  P 1 3 5          remplacer la sélection par 1, 3, 5 puis jouer\n"
				+ "  D (ou discard)   jeter la sélection courante (re-pioche)\n"
				+ "  D 2 4            remplacer la sélection par 2, 4 puis jeter\n"
				+ "  C                vider la sélection\n" + "  R                trier la main par rang (2 → A)\n"
				+ "  S                trier la main par suit (♣ ♥ ♠ ♦)\n" + "  H ou ?           cette aide\n"
				+ "  Q                quitter\n\n" + BOLD + "Commandes shop" + RESET + "\n"
				+ "  [1-N]      acheter l'offre N ($" + Shop.PLANET_PRICE + ")\n"
				+ "  E          quitter le shop (passe au round suivant)\n\n" + BOLD
				+ "Hiérarchie des mains (plus faible → plus fort)" + RESET + "\n" + "  HIGH_CARD         carte isolée\n"
				+ "  PAIR              2 cartes de même rang\n" + "  TWO_PAIR          2 paires\n"
				+ "  THREE_OF_A_KIND   3 cartes de même rang\n"
				+ "  STRAIGHT          5 rangs consécutifs (A-2-3-4-5 et 10-J-Q-K-A OK)\n"
				+ "  FLUSH             5 cartes de même couleur\n" + "  FULL_HOUSE        brelan + paire\n"
				+ "  FOUR_OF_A_KIND    4 cartes de même rang\n" + "  STRAIGHT_FLUSH    straight + flush\n"
				+ "  ROYAL_FLUSH       10-J-Q-K-A même couleur\n\n" + DIM + "(appuyer Entrée pour revenir)" + RESET);
		getUserInput(null);
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
		System.out.println("Score:   " + BOLD + result.score().chips() + RESET + " chips × " + BOLD
				+ result.score().mult() + RESET + " mult = " + BOLD + GREEN + result.score().total() + RESET);
		System.out.println();

		int target = state.getBlinds().get((state.getRound() - 1) % 3).score();
		int totalScore = state.getCurrentBlindScore();
		GamePhase phase = state.getPhase();
		String prompt;
		if (phase == GamePhase.SHOP || phase == GamePhase.VICTORY) {
			System.out.println(BOLD + GREEN + "★ BLIND BATTUE !  " + totalScore + " / " + target + "  ★" + RESET);
			prompt = "(Entrée pour " + (phase == GamePhase.VICTORY ? "voir la victoire" : "entrer dans le shop") + ")";
		} else if (phase == GamePhase.GAME_OVER) {
			System.out.println(BOLD + RED + "× ÉCHEC : " + totalScore + " / " + target + "  (manqué de "
					+ (target - totalScore) + ") ×" + RESET);
			prompt = "(Entrée pour continuer)";
		} else {
			int handsLeft = state.getMaxHands() - state.getCurrentHandsPlay();
			System.out.println("Cumul: " + BOLD + totalScore + RESET + " / " + target + "    Mains restantes: "
					+ handsLeft + "/" + state.getMaxHands());
			prompt = "(Entrée pour continuer)";
		}
		System.out.println();
		System.out.println(DIM + prompt + RESET);
	}

	// ─── Helpers de rendu privés ───────────────────────────────────────────

	private void renderPlaying(GameState state, int blindIndex, int target, Optional<PlayedHand> preview) {

		System.out.println(BOLD + CYAN + "═══ Tribala CLI ═══" + RESET);
		System.out.println("Ante " + state.getAnte() + " · Round " + state.getRound() + " · " + BOLD
				+ BLIND_LABELS[blindIndex] + RESET);
		System.out.println("Score: " + BOLD + state.getCurrentBlindScore() + RESET + " / " + target + "    Hands: "
				+ state.getCurrentHandsPlay() + "/" + state.getMaxHands() + "    Discards: "
				+ state.getCurrentDiscards() + "/" + state.getMaxDiscards() + "    " + YELLOW + "$" + state.getDollars()
				+ RESET);
		System.out.println();

		List<Card> hand = state.getCurrentHand();
		List<Card> selected = state.getSelectedCards();
		System.out.println(BOLD + "Hand:" + RESET);
		StringBuilder sb = new StringBuilder("  ");
		for (int i = 0; i < hand.size(); i++) {
			Card c = hand.get(i);
			boolean isSel = selected.contains(c);
			sb.append(DIM).append("[").append(i + 1).append("]").append(RESET).append(" ").append(formatCard(c, isSel))
					.append("   ");
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
			preview.ifPresent(
					p -> System.out.println("  " + DIM + "→ " + RESET + BOLD + formatHandType(p.type()) + RESET));
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
				System.out.println("  " + DIM + "[" + (i + 1) + "]" + RESET + " " + BOLD + p.name() + RESET + DIM
						+ "  ($" + Shop.PLANET_PRICE + ")" + RESET);
				System.out.println("      " + BOLD + formatHandType(target) + RESET + DIM + "  lvl " + level + " → "
						+ (level + 1) + RESET + "    " + GREEN + "+" + target.levelChips() + " chips, +"
						+ target.levelMult() + " mult" + RESET);
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

	private void renderGameOver(GameState state, int blindIndex, int target) {
		System.out.println(BOLD + RED + "═══ GAME OVER ═══" + RESET);
		System.out.println("Vaincu par " + BOLD + BLIND_LABELS[blindIndex] + RESET + " (Ante " + state.getAnte() + ")");
		System.out.println("Score final: " + state.getCurrentBlindScore() + " / " + target + "  (manqué de "
				+ (target - state.getCurrentBlindScore()) + ")");
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
