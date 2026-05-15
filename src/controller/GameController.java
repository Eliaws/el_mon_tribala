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
		for (Card c : gameState.getCurrentDeck().getCard(gameState.getHandSize() - hand.size())) {
			hand.add(c);
		}
	}

	public boolean select(int index) {
		var hand = gameState.getCurrentHand();
		if (index >= hand.size()) {
			return false;
		}
		var selectedCards = gameState.getSelectedCards();
		selectedCards.add(hand.get(index));
		return true;
	}

	public boolean unselect(int index) {
		var hand = gameState.getCurrentHand();
		if (index >= hand.size()) {
			return false;
		}
		var selectedCards = gameState.getSelectedCards();
		selectedCards.remove(hand.get(index));
		return true;
	}

	public boolean canDiscard() {
		if (gameState.getCurrentDiscards() < gameState.getMaxDiscards()) {
			return true;
		}
		return false;
	}

	public void discard() {
		var selectedCards = gameState.getSelectedCards();
		var deck = gameState.getCurrentDeck();
		deck.discard(selectedCards);
		gameState.getCurrentHand().removeAll(selectedCards);
		while (selectedCards.size() > 0) {
			selectedCards.remove(0);
		}
		gameState.setCurrentDiscards(gameState.getCurrentDiscards() + 1);
		draw();
	}

	public boolean canPlay() {
		if (gameState.getCurrentHandsPlay() < gameState.getMaxHands()) {
			return true;
		}
		return false;
	}

	public void play() {
		var selectedCards = gameState.getSelectedCards();
		var played = HandEvaluator.evaluate(selectedCards);
		var addedScore = HandScorer.score(played, gameState.getHandLevels());
		var stats = gameState.getPlayedHandStats();
		stats.merge(played.type(), 1, Integer::sum);
		var currentScore = gameState.getCurrentBlindScore();
		gameState.setCurrentBlindScore(addedScore.total() + currentScore);
		gameState.getCurrentDeck().discard(selectedCards);
		gameState.getCurrentHand().removeAll(selectedCards);
		while (selectedCards.size() > 0) {
			selectedCards.remove(0);
		}
		gameState.setCurrentHandsPlay(gameState.getCurrentHandsPlay() + 1);
		draw();
	}

	public boolean isCurrentBlindWon() {
		var currentBlind = gameState.getBlinds().get((gameState.getRound() - 1) % 3);
		var currentBlindScore = gameState.getCurrentBlindScore();
		if (currentBlindScore >= currentBlind.score()) {
			return true;
		}
		return false;
	}

	public void finishBlind() {
		var hand = gameState.getCurrentHand();
		gameState.getCurrentDeck().discard(hand);
		while (hand.size() > 0) {
			hand.remove(0);
		}
		gameState.setCurrentBlindScore(0);
		gameState.setCurrentDiscards(0);
		gameState.setCurrentHandsPlay(0);
		gameState.setRound(gameState.getRound() + 1);
		gameState.setAnte(((gameState.getRound() - 1) / 3) + 1);
		if (((gameState.getRound() - 1) % 3) == 0) {
			gameState.setBlinds();
		}
	}
}
