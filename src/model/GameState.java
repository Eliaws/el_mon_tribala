package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import controller.PlayResult;
import domain.Blind;
import domain.Card;
import domain.Deck;
import domain.Shop;
import domain.consummables.Planet;
import domain.hand.HandType;
import domain.hand.combinations.PlayedHand;

public class GameState {

	private int maxHandSize;
	private int maxHandsPlay;
	private int currentHandsPlay;
	private int maxDiscards;
	private int currentDiscard;
	private int maxSelected;
//	private int maxJokers;

	private boolean sortedByRank;
	private boolean sortedBySuit;

	private int ante;
	private int round;
	private int currentBlindScore;
	private final List<Blind> blinds;
	private final Deck currentDeck;

	private int dollars;
	private final Shop shop;

	private final List<Card> currentHand;
	private final List<Card> selectedCards;
	private final Map<HandType, Integer> handLevels;
	private final Map<HandType, Integer> playedHandStats;
	private Optional<PlayedHand> previewHand;
	private PlayResult lastResult;
	private String message;
//  private final List<Joker> currentJokers;

	private GamePhase phase;

	public GameState() {
		this.maxHandSize = 8;
		this.maxHandsPlay = 4;
		this.currentHandsPlay = 0;
		this.maxDiscards = 3;
		this.currentDiscard = 0;
		this.maxSelected = 5;

		this.sortedByRank = true;
		this.sortedBySuit = false;

		this.currentBlindScore = 0;
		this.ante = 1;
		this.round = 1;
		this.dollars = 4;
		this.phase = GamePhase.PLAYING_BLIND;
		this.blinds = new ArrayList<Blind>();

		this.currentDeck = new Deck();
		this.shop = new Shop();
		this.currentHand = new ArrayList<Card>();
		this.selectedCards = new ArrayList<Card>();

		this.handLevels = new HashMap<HandType, Integer>();
		this.playedHandStats = new HashMap<HandType, Integer>();
		this.previewHand = Optional.empty();

		this.currentDeck.generateBaseDeck();
		this.setBlinds();
	}

	public List<Blind> getBlinds() {
		return blinds;
	}

	/**
	 * Initialize or replaces the content of the blinds list with 3 blinds based on
	 * the ante.
	 */
	public void setBlinds() {
		blinds.clear();
		ArrayList<Integer> anteRequirements = new ArrayList<Integer>(
				List.of(100, 300, 800, 2000, 5000, 11000, 20000, 35000, 50000));
		double currentAnteBase;
		if (ante <= 8) {
			currentAnteBase = anteRequirements.get(ante - 1);
		} else {
			currentAnteBase = (anteRequirements.get(7)
					* Math.pow(1.6 + Math.pow(0.75 * (ante - 8), 1 + 0.2 * (ante - 8)), ante - 8));
		}

		this.blinds.add(new Blind((int) currentAnteBase, 3));
		this.blinds.add(new Blind((int) (currentAnteBase * 1.5), 4));
		this.blinds.add(new Blind((int) (currentAnteBase * 2), 5));
	}

	/**
	 * Increments the level of the handType corresponding to the given Planet
	 * HandType.
	 * 
	 * @param p the Planet whose HandType level should be incremented
	 */
	public void addPlanet(Planet p) {
		Objects.requireNonNull(p);
		this.handLevels.merge(p.getTarget(), 1, Integer::sum);
	}

	/**
	 * Returns a Map with for each handType it's current level.
	 * 
	 * @return Map<HandType, Integer> with the current level of each handType
	 */
	public Map<HandType, Integer> getHandLevels() {
		return handLevels;
	}

	/**
	 * Returns the maximum size of the player's hand.
	 * 
	 * @return the maximum hand size
	 */
	public int getHandSize() {
		return maxHandSize;
	}

	/**
	 * Returns the current maximum number of hands the player can play for each
	 * blind.
	 * 
	 * @return the maximum number of hands that can be played.
	 */
	public int getMaxHands() {
		return maxHandsPlay;
	}

	/**
	 * Returns the maximum number of times the player can discard cards during a
	 * blind.
	 * 
	 * @return the maximum number of discards.
	 */
	public int getMaxDiscards() {
		return maxDiscards;
	}

	/**
	 * Returns the maximum number of cards the player can select in their hand.
	 * 
	 * @return the maximum number of selected cards.
	 */
	public int getMaxSelected() {
		return maxSelected;
	}

	/**
	 * Returns the current ante, which determines the stakes of the game and
	 * influences the blinds.
	 * 
	 * @return the current ante value.
	 */
	public int getAnte() {
		return ante;
	}

	/**
	 * Sets the ante to the given value, which will influence the stakes of the game
	 * and the values of the blinds.
	 * 
	 * @param ante the new ante value to set.
	 */
	public void setAnte(int ante) {
		this.ante = ante;
	}

	/**
	 * Returns whether the player's hand is currently sorted by rank.
	 * 
	 * @return true if the hand is sorted by rank, false otherwise.
	 */
	public boolean isSortedByRank() {
		return sortedByRank;
	}

	/**
	 * Returns whether the player's hand is currently sorted by suit.
	 * 
	 * @return true if the hand is sorted by suit, false otherwise.
	 */
	public boolean isSortedBySuit() {
		return sortedBySuit;
	}

	public void setSortedByRank(boolean sortedByRank) {
		this.sortedByRank = sortedByRank;
	}

	public void setSortedBySuit(boolean sortedBySuit) {
		this.sortedBySuit = sortedBySuit;
	}

	/**
	 * Returns the current round number, which indicates the number of blinds played
	 * in the game.
	 * 
	 * @return the current round number
	 */
	public int getRound() {
		return round;
	}

	/**
	 * Sets the current round number to the given value.
	 * 
	 * @param round the new round number to set
	 */
	public void setRound(int round) {
		this.round = round;
	}

	/**
	 * Returns the current hand of the player, which is a list of Card objects
	 * representing the cards in the player's hand.
	 * 
	 * @return the current hand
	 */
	public List<Card> getCurrentHand() {
		return currentHand;
	}

	/**
	 * Returns the current deck of cards, which is a Deck object representing the
	 * cards available for drawing during the game.
	 * 
	 * @return the current deck
	 */
	public Deck getCurrentDeck() {
		return currentDeck;
	}

	/**
	 * Returns a Map containing the statistics of the hands played by the player,
	 * where the keys are HandType objects representing the type of hand and the
	 * values are integers representing the number of times that hand type has been
	 * played.
	 * 
	 * @return a Map with the statistics of the hands played by the player
	 */
	public Map<HandType, Integer> getPlayedHandStats() {
		return playedHandStats;
	}

	/**
	 * Returns the list of cards currently selected by the player.
	 * 
	 * @return the list of selected cards
	 */
	public List<Card> getSelectedCards() {
		return selectedCards;
	}

	/**
	 * Returns the score for the current blind.
	 * 
	 * @return the current blind score
	 */
	public int getCurrentBlindScore() {
		return currentBlindScore;
	}

	/**
	 * Sets the score for the current blind to the given value.
	 * 
	 * @param score the new score for the current blind
	 */
	public void setCurrentBlindScore(int score) {
		currentBlindScore = score;
	}

	/**
	 * Returns the number of discards left in the current blind.
	 * 
	 * @return the number of discards left
	 */
	public int getCurrentDiscards() {
		return currentDiscard;
	}

	/**
	 * Sets the number of discards left in the current blind to the given value.
	 * 
	 * @param number the new number of discards left
	 */
	public void setCurrentDiscards(int number) {
		currentDiscard = number;
	}

	/**
	 * Returns the number of hands the player has played in the current blind.
	 * 
	 * @return the number of hands played in the current blind
	 */
	public int getCurrentHandsPlay() {
		return currentHandsPlay;
	}

	/**
	 * Sets the number of hands the player has played in the current blind to the
	 * given value.
	 * 
	 * @param number the new number of hands played in the current blind
	 */
	public void setCurrentHandsPlay(int number) {
		currentHandsPlay = number;
	}

	/**
	 * Returns the amount of money the player currently has.
	 * 
	 * @return the amount of money
	 */
	public int getDollars() {
		return dollars;
	}

	/**
	 * Adds the specified amount to the player's money.
	 * 
	 * @param amount the amount to add
	 */
	public void addDollars(int amount) {
		this.dollars += amount;
	}

	/**
	 * Returns the current phase of the game, which indicates whether the player is
	 * currently playing a blind, in the shop, won or lost the game.
	 * 
	 * @return the current phase
	 */
	public GamePhase getPhase() {
		return phase;
	}

	/**
	 * Sets the current phase of the game to the given value, which indicates
	 * whether the player is currently playing a blind, in the shop, won or lost the
	 * game.
	 * 
	 * @param phase the new phase of the game
	 */
	public void setPhase(GamePhase phase) {
		Objects.requireNonNull(phase);
		this.phase = phase;
	}
	
	public void setPreviewHand(Optional<PlayedHand> preview) {
		this.previewHand = preview;
	}
	
	public Optional<PlayedHand> getPreviewHand() {
		return this.previewHand;
	}
	
	public void setLastResult(PlayResult lastResult) {
		this.lastResult = lastResult;
	}
	
	public PlayResult getLastResult() {
		return this.lastResult;
	}

	/**
	 * Message transitoire (erreur de saisie, info) à afficher au prochain rendu.
	 * Consommé une seule fois : la vue l'efface après affichage.
	 *
	 * @return le message courant, ou null si aucun
	 */
	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Returns the Shop object representing the in-game shop where the player can
	 * buy planets and other items.
	 * 
	 * @return the shop object
	 */
	public Shop getShop() {
		return shop;
	}
}
