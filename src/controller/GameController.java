package controller;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import domain.Blind;
import domain.Card;
import domain.Shop;
import domain.consummables.Planet;
import domain.hand.combinations.PlayedHand;
import domain.hand.evaluation.HandEvaluator;
import domain.hand.scoring.HandScorer;
import domain.hand.scoring.Score;
import model.GamePhase;
import model.GameState;
import view.View;

public class GameController {

	View view;
	GameState gameState;
	private boolean running;

	public GameController(View view) {
		this.gameState = new GameState();
		this.view = view;
		this.running = true;
	}

	public GameState getGameState() {
		return gameState;
	}

	public void run() {
		start();
		while (running) {
			view.render(gameState);
			switch (gameState.getPhase()) {
			case PLAYING_BLIND -> handlePlay();
			case FINISHED_BLIND -> handleFinishedBlind();
			case SHOP -> handleShop();
			case GAME_OVER, VICTORY -> handleEnd();
			}
		}
	}

	private void handlePlay() {
		List<String> input = view.getUserInput(gameState);
		if (input == null || input.isEmpty()) {
			return;
		}
		String first = input.get(0);

		switch (first) {
		case "p":
		case "play":
			play();
			return;
		case "d":
		case "discard":
			discard();
			return;
		case "c":
		case "clear":
			clearSelection();
			return;
		case "r":
		case "rank":
			sortHandByRank();
			return;
		case "s":
		case "suit":
			sortHandBySuit();
			return;
		case "h":
		case "help":
		case "?":
			view.renderHelp();
			return;
		case "q":
			running = false;
			return;
		case "invalid":
			view.renderInvalidInput(gameState, input.get(1));
			return;
		}

		for (String s : input) {
			int index = Integer.parseInt(s);
			toggle(index);
		}

	}

	private void handleFinishedBlind() {
		List<String> input = view.getUserInput(gameState);
		if(input == null) {
			return;
		}
		if (input.isEmpty()) {
			winBlind();
			return;
		}
	}
	
	private void handleShop() {
		List<String> input = view.getUserInput(gameState);
		if (input == null || input.isEmpty()) {
			return;
		}
		String first = input.get(0);

		switch (first) {
		case "e":
		case "exit":
			exitShop();
			return;
		case "h":
		case "help":
		case "?":
			view.renderHelp();
			return;
		case "q":
			running = false;
			return;
		}

		char c = input.get(0).charAt(0);
		if (Character.isDigit(c)) {
			Integer n = Integer.parseInt(input.get(0));
			if (!buyPlanet(n)) {
				int price = Shop.PLANET_PRICE;
				if (gameState.getDollars() < price) {
					view.renderInvalidInput(gameState, "Pas assez de dollars (besoin $" + price + ")");
				} else {
					view.renderInvalidInput(gameState, "Offre indisponible");
				}
			}
		}

	}

	private void handleEnd() {
		List<String> input = view.getUserInput(gameState);
		if (input.isEmpty()) {
			return;
		}
		String first = input.get(0);
		switch (first) {
		case "r":
		case "restart":
			restart();
			return;
		case "h":
		case "help":
		case "?":
			view.renderHelp();
			return;
		case "q":
			running = false;
			return;
		default:
			view.renderInvalidInput(gameState, first);
		}
	}

	/**
	 * Démarre la partie : pioche la main initiale. À appeler une fois après
	 * construction du controller, avant la première itération de la vue.
	 */
	public void start() {
		draw();
	}

	/**
	 * Réinitialise la partie : nouveau {@link GameState} et pioche initiale.
	 */
	public void restart() {
		this.gameState = new GameState();
		draw();
	}

	/**
	 * Draws cards from the deck to the hand until the hand is full. If the hand is
	 * already full, does nothing. If the hand is sorted, sorts the hand after
	 * drawing.
	 */
	public void draw() {
		var hand = gameState.getCurrentHand();
		for (Card c : gameState.getCurrentDeck().getCard(gameState.getHandSize() - hand.size())) {
			hand.add(c);
		}
		if (gameState.isSortedByRank()) {
			sortHandByRank();
		} else if (gameState.isSortedBySuit()) {
			sortHandBySuit();
		}
	}

	/**
	 * Sets the current sort order to rank and sorts the player's hand by rank.
	 */
	public void sortHandByRank() {
		if (gameState.getPhase() != GamePhase.PLAYING_BLIND) {
			return;
		}
		gameState.setSortedByRank(true);
		gameState.setSortedBySuit(false);
		gameState.getCurrentHand().sort(Comparator.comparingInt((Card c) -> c.rank().ordinal()));
	}

	/**
	 * Sets the current sort order to suit and sorts the player's hand by suit.
	 */
	public void sortHandBySuit() {
		if (gameState.getPhase() != GamePhase.PLAYING_BLIND) {
			return;
		}
		gameState.setSortedBySuit(true);
		gameState.setSortedByRank(false);
		gameState.getCurrentHand().sort(
				Comparator.comparingInt((Card c) -> c.suit().ordinal()).thenComparingInt(c -> c.rank().ordinal()));
	}

	/**
	 * Sélectionne ou désélectionne la carte de la main à l'index donné selon son
	 * état actuel.
	 *
	 * @param index index dans la main (0-based)
	 * @return true si une action a eu lieu, false si rien n'a changé (cap atteint,
	 *         index invalide…)
	 */
	public boolean toggle(int index) {
		if (gameState.getPhase() != GamePhase.PLAYING_BLIND) {
			return false;
		}
		var hand = gameState.getCurrentHand();
		if (index < 0 || index >= hand.size()) {
			return false;
		}
		Card card = hand.get(index);
		if (gameState.getSelectedCards().contains(card)) {
			return unselect(index);
		}
		return select(index);
	}

	/**
	 * Selects a card from the player's hand to be discarded or played. Returns true
	 * if the card was successfully selected, false otherwise.
	 * 
	 * @param index the index of the card in the player's hand to be selected
	 * @return true if the card was successfully selected, false otherwise
	 */
	public boolean select(int index) {
		if (gameState.getPhase() != GamePhase.PLAYING_BLIND) {
			return false;
		}
		var hand = gameState.getCurrentHand();
		if (index < 0 || index >= hand.size()) {
			return false;
		}
		var selectedCards = gameState.getSelectedCards();
		if (selectedCards.size() >= gameState.getMaxSelected()) {
			return false;
		}
		Card card = hand.get(index);
		if (selectedCards.contains(card)) {
			return false;
		}
		selectedCards.add(card);
		setPreviewSelection();
		return true;
	}

	/**
	 * Unselects a card from the player's hand that was previously selected to be
	 * discarded or played. Returns true if the card was successfully unselected,
	 * false otherwise.
	 * 
	 * @param index the index of the card in the player's hand to be unselected
	 * @return true if the card was unselected, false otherwise
	 */
	public boolean unselect(int index) {
		if (gameState.getPhase() != GamePhase.PLAYING_BLIND) {
			return false;
		}
		var hand = gameState.getCurrentHand();
		if (index < 0 || index >= hand.size()) {
			return false;
		}
		var selectedCards = gameState.getSelectedCards();
		selectedCards.remove(hand.get(index));
		setPreviewSelection();
		return true;
	}

	/**
	 * Vide la sélection courante.
	 */
	public void clearSelection() {
		gameState.getSelectedCards().clear();
	}

	/**
	 * Évalue la sélection courante sans modifier l'état.
	 * 
	 * @return la main poker correspondant à la sélection, ou empty si rien n'est
	 *         sélectionné
	 */
	public void setPreviewSelection() {
		var selected = gameState.getSelectedCards();
		if (selected.isEmpty()) {
			this.gameState.setPreviewHand(Optional.empty());
		} else {
			this.gameState.setPreviewHand(Optional.of(HandEvaluator.evaluate(selected)));
		}
	}

	/**
	 * Index de la blinde courante dans la liste {@code state.getBlinds()} : 0 =
	 * Small, 1 = Big, 2 = Boss.
	 */
	public int getCurrentBlindIndex() {
		return (gameState.getRound() - 1) % 3;
	}

	/**
	 * Renvoie la blinde courante (Small/Big/Boss du round en cours).
	 */
	public Blind getCurrentBlind() {
		return gameState.getBlinds().get(getCurrentBlindIndex());
	}

	/**
	 * Checks if the player can discard cards. The player can discard if the current
	 * phase is PLAYING_BLIND and the number of discards made is less than the
	 * maximum allowed discards.
	 * 
	 * @return true if the player can discard cards, false otherwise
	 */
	public boolean canDiscard() {
		if (gameState.getPhase() != GamePhase.PLAYING_BLIND) {
			return false;
		}
		if (gameState.getCurrentDiscards() < gameState.getMaxDiscards()) {
			return true;
		}
		return false;
	}

	/**
	 * Discards the selected cards from the player's hand and adds them to the
	 * discard pile of the deck. Increments the current discards count and draws new
	 * cards to replace the discarded ones.
	 */
	public void discard() {
		if (gameState.getPhase() != GamePhase.PLAYING_BLIND) {
			return;
		}
		var selectedCards = gameState.getSelectedCards();
		var deck = gameState.getCurrentDeck();
		deck.discard(selectedCards);
		gameState.getCurrentHand().removeAll(selectedCards);
		selectedCards.clear();
		gameState.setCurrentDiscards(gameState.getCurrentDiscards() + 1);
		setPreviewSelection();
		draw();
	}

	/**
	 * Checks if the player can play their hand. The player can play if the current
	 * phase is PLAYING_BLIND and the number of hands played is less than the
	 * maximum allowed hands.
	 * 
	 * @return true if the player can play their hand, false otherwise
	 */
	public boolean canPlay() {
		if (gameState.getPhase() != GamePhase.PLAYING_BLIND) {
			return false;
		}
		if (gameState.getCurrentHandsPlay() < gameState.getMaxHands()) {
			return true;
		}
		return false;
	}

	/**
	 * Joue la sélection courante : évalue la main, score les chips, met à jour le
	 * state et déclenche la transition de phase appropriée (shop si la blinde
	 * tombe, game over si les mains sont épuisées, victoire si on bat le boss de
	 * l'ante 8).
	 *
	 * @return le détail de la main jouée (type, score, cartes jouées) ou
	 *         {@code null} si l'action n'a pas pu être effectuée (mauvaise phase,
	 *         sélection vide)
	 */
	public void play() {
		if (gameState.getPhase() != GamePhase.PLAYING_BLIND) {
			return;
		}
		var selectedCards = gameState.getSelectedCards();
		if (selectedCards.isEmpty()) {
			return;
		}
		List<Card> playedCards = List.copyOf(selectedCards);
		PlayedHand played = HandEvaluator.evaluate(playedCards);
		Score addedScore = HandScorer.score(played, gameState.getHandLevels());
		var stats = gameState.getPlayedHandStats();
		stats.merge(played.type(), 1, Integer::sum);
		gameState.setCurrentBlindScore(addedScore.total() + gameState.getCurrentBlindScore());
		gameState.getCurrentDeck().discard(selectedCards);
		gameState.getCurrentHand().removeAll(selectedCards);
		selectedCards.clear();
		gameState.setCurrentHandsPlay(gameState.getCurrentHandsPlay() + 1);

		if (isCurrentBlindWon()) {
			if (isGameWon()) {
				winGame();
			} else {
				gameState.setLastResult(new PlayResult(played, addedScore, playedCards));
				gameState.setPhase(GamePhase.FINISHED_BLIND);
			}
		} else if (isCurrentBlindLost()) {
			looseBlind();
		} else {
			draw();
		}
	}

	/**
	 * Checks if the player has won the current blind.
	 * 
	 * @return true if the player's current blind is won, false otherwise
	 */
	public boolean isCurrentBlindWon() {
		var currentBlind = getCurrentBlind();
		var currentBlindScore = gameState.getCurrentBlindScore();
		if (currentBlindScore >= currentBlind.score()) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if the player has lost the current blind.
	 * 
	 * @return true if the player's current blind is lost, false otherwise
	 */
	public boolean isCurrentBlindLost() {
		boolean noHandsLeft = gameState.getCurrentHandsPlay() >= gameState.getMaxHands();
		if (noHandsLeft && !isCurrentBlindWon()) {
			return true;
		}
		return false;
	}

	/**
	 * Prepares the game state for the next round after winning a blind. Rewards the
	 * player with dollars based on the blind's reward, a hand bonus, and an
	 * interest bonus. Then enters the shop phase.
	 */
	private void winBlind() {
		var blind = getCurrentBlind();
		int handBonus = gameState.getMaxHands() - gameState.getCurrentHandsPlay();
		int interest = Math.min(gameState.getDollars() / 5, 5);
		gameState.addDollars(blind.reward() + handBonus + interest);
		setPreviewSelection();
		enterShop();
	}

	/**
	 * Checks if the player has won the game. The player wins the game if they have
	 * won more than 8 ante, or if they have won the last blind of the 8th ante.
	 * 
	 * @return true if the player has won the game, false otherwise
	 */
	public boolean isGameWon() {
		if (gameState.getAnte() > 8) {
			return true;
		} else if ((gameState.getRound() - 1) % 3 == 2 && gameState.getAnte() == 8) {
			return isCurrentBlindWon();
		}
		return false;
	}

	private void winGame() {
		gameState.setPhase(GamePhase.VICTORY);
	}

	private void looseBlind() {
		gameState.setPhase(GamePhase.GAME_OVER);
	}

	private void enterShop() {
		gameState.getShop().regenerate();
		gameState.setPhase(GamePhase.SHOP);
	}

	/**
	 * Allows the player to buy a planet from the shop if they have enough dollars
	 * and if the offer index is valid.
	 * 
	 * @param offerIndex
	 * @return true if the planet was successfully bought, false otherwise
	 */
	public boolean buyPlanet(int offerIndex) {
		if (gameState.getPhase() != GamePhase.SHOP) {
			return false;
		}
		Shop shop = gameState.getShop();
		if (offerIndex < 0 || offerIndex >= shop.getOffers().size()) {
			return false;
		}
		if (gameState.getDollars() < Shop.PLANET_PRICE) {
			return false;
		}
		Planet planet = shop.getOffers().get(offerIndex);
		IO.println(planet.name());
		gameState.addDollars(-Shop.PLANET_PRICE);
		gameState.addPlanet(planet);
		shop.remove(offerIndex);
		return true;
	}

	/**
	 * Exits the shop phase and prepares the game state for the next round. Discards
	 * the player's hand, resets the current blind score, discards count, and hands
	 * played count. Increments the round and updates the ante and blinds if
	 * necessary. Then enters the playing blind phase.
	 */
	public void exitShop() {
		if (gameState.getPhase() != GamePhase.SHOP) {
			return;
		}
		var hand = gameState.getCurrentHand();
		gameState.getCurrentDeck().discard(hand);
		hand.clear();
		gameState.setCurrentBlindScore(0);
		gameState.setCurrentDiscards(0);
		gameState.setCurrentHandsPlay(0);
		gameState.setRound(gameState.getRound() + 1);
		gameState.setAnte(((gameState.getRound() - 1) / 3) + 1);
		if (((gameState.getRound() - 1) % 3) == 0) {
			gameState.setBlinds();
		}
		gameState.setPhase(GamePhase.PLAYING_BLIND);
		draw();
	}
}
