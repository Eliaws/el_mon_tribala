package controller;

import domain.Card;
import domain.hand.evaluation.HandEvaluator;
import domain.hand.scoring.HandScorer;
import model.GameState;

public class GameController {
	GameState gameState;
	
	
	public GameController() {
		this.gameState = new GameState();
	}
	
	public void draw() {
		var hand = gameState.getCurrentHand();
		for (Card c: gameState.getCurrentDeck().getCard(gameState.getHandSize()-hand.size())) {
			hand.add(c);
		}
	}

	public boolean choose(int index) {
		var choosenCards = gameState.getChoosenCards();
		var hand = gameState.getCurrentHand();
		if(index >= hand.size()) {
			return false;
		}
		choosenCards.add(hand.remove(index));
		return true;
	}
	
	public void discard() {
		var choosenCards = gameState.getChoosenCards();
		var deck = gameState.getCurrentDeck();
		deck.discard(choosenCards);
		while(choosenCards.size()>0) {
			choosenCards.remove(0);
		}
	}
	
	public boolean play() {
		var choosenCards = gameState.getChoosenCards();
		if(choosenCards.size() == 0) {
			return false;
		}
		var played = HandEvaluator.evaluate(choosenCards);
		var addedScore = HandScorer.score(played, gameState.getPlanets());
		var currentScore = gameState.getCurrentBlindScore();
		gameState.setCurrentBLindScore(addedScore.chips()*addedScore.mult()+currentScore);
		return true;
	}
}
