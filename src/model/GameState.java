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
	private int maxDiscardsPlay;
//	private int maxJokers;
	private int ante;
	private int round;
    
	private final List<Blind> blinds;
	private final Deck currentDeck;

//  List<Joker> currentJokers;
	private final List<Card> currentHand;
	private final Map<Planet,Integer> planets;
    private final Map<PlayedHand, Integer> playedhandStats;
    
    public GameState() {
    	this.maxHandSize = 8;
    	this.maxHandsPlay = 4;
    	this.maxDiscardsPlay = 3;
    	this.ante = 1;
    	
    	this.blinds = new ArrayList<Blind>();
    	this.currentDeck = new Deck();    	
    	this.currentHand = new ArrayList<Card>();
    	this.planets = new HashMap<Planet,Integer>();
    	this.playedhandStats = new HashMap<PlayedHand,Integer>();

    	this.currentDeck.generateBaseDeck();
    }
    
    public List<Blind> getBlinds() {
    	return blinds;
    }
    
    public void setBlinds() {
    	while(this.blinds.size() > 0) {
    		this.blinds.remove(0);
    	}
    	// TODO: change score formula
    	this.blinds.add(new Blind(100*ante));
    	this.blinds.add(new Blind((int)(100*ante*1.5)));
    	this.blinds.add(new Blind((int)(100*ante*2)));
    }
    
    public void addPlanet(Planet p) {
    	this.planets.put(p, this.planets.getOrDefault(p, 0) + 1);
    }
    
    public int getHandSize() {
    	return maxHandSize;
    }
    
    public int getMaxHandsPlay() {
    	return maxHandsPlay;
    }
    
    public int getMaxDiscardsPlay() {
    	return maxDiscardsPlay;
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
    
}
