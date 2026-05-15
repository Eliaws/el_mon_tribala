package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import domain.Blind;
import domain.Card;
import domain.Deck;
import domain.consummables.Planet;
import domain.hand.combinations.PlayedHand;

public class GameState {

	private int maxHandSize;
	private int maxHandsPlay;
	private int currentHandsPlay;
	private int maxDiscards;
	private int currentDiscard;
//	private int maxJokers;
	private int ante;
	private int round;
	private int currentBlindScore;

	private final List<Blind> blinds;
	private final Deck currentDeck;

//  List<Joker> currentJokers;
	private final List<Card> currentHand;
	private final List<Card> selectedCards;
	private final Map<Planet, Integer> planets;
	private final Map<PlayedHand, Integer> playedHandStats;

	public GameState() {
		this.maxHandSize = 8;
		this.maxHandsPlay = 5;
		this.currentHandsPlay = 0;
		this.maxDiscards = 3;
		this.currentDiscard = 0;

		this.currentBlindScore = 0;
		this.ante = 1;
		this.round = 1;
		this.blinds = new ArrayList<Blind>();

		this.currentDeck = new Deck();
		this.currentHand = new ArrayList<Card>();
		this.selectedCards = new ArrayList<Card>();

		this.planets = new HashMap<Planet, Integer>();
		this.playedHandStats = new HashMap<PlayedHand, Integer>();

		this.currentDeck.generateBaseDeck();
	}

	public List<Blind> getBlinds() {
		return blinds;
	}

	public void setBlinds() {
		while (this.blinds.size() > 0) {
			this.blinds.remove(0);
		}
		// TODO: change score formula
		this.blinds.add(new Blind(100 * ante));
		this.blinds.add(new Blind((int) (100 * ante * 1.5)));
		this.blinds.add(new Blind((int) (100 * ante * 2)));
	}

	public void addPlanet(Planet p) {
		this.planets.put(p, this.planets.getOrDefault(p, 0) + 1);
	}

	public Map<Planet, Integer> getPlanets() {
		return planets;
	}

	public int getHandSize() {
		return maxHandSize;
	}

	public int getMaxHands() {
		return maxHandsPlay;
	}

	public int getMaxDiscards() {
		return maxDiscards;
	}

	public int getAnte() {
		return ante;
	}

	public void setAnte(int ante) {
		this.ante = ante;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public List<Card> getCurrentHand() {
		return currentHand;
	}

	public Deck getCurrentDeck() {
		return currentDeck;
	}

	public Map<PlayedHand, Integer> getPlayedHandStats() {
		return playedHandStats;
	}

	public List<Card> getSelectedCards() {
		return selectedCards;
	}

	public int getCurrentBlindScore() {
		return currentBlindScore;
	}

	public void setCurrentBLindScore(int score) {
		currentBlindScore = score;
	}

	public int getCurrentDiscards() {
		return currentDiscard;
	}

	public void setCurrentDiscards(int number) {
		currentDiscard = number;
	}

	public int getCurrentHandsPlay() {
		return currentHandsPlay;
	}

	public void setCurrentHandsPlay(int number) {
		currentHandsPlay = number;
	}
}
