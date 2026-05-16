package controller;

import domain.Card;
import domain.Shop;
import domain.consummables.Planet;
import domain.hand.evaluation.HandEvaluator;
import domain.hand.scoring.HandScorer;
import model.GamePhase;
import model.GameState;

public class GameController {
	GameState gameState;

	public GameController() {
		this.gameState = new GameState();
	}

	public GameState getGameState() {
		return gameState;
	}

	public void draw() {
		var hand = gameState.getCurrentHand();
		for (Card c : gameState.getCurrentDeck().getCard(gameState.getHandSize() - hand.size())) {
			hand.add(c);
		}
	}

	public boolean select(int index) {
		if (gameState.getPhase() != GamePhase.PLAYING_BLIND) {
			return false;
		}
		var hand = gameState.getCurrentHand();
		if (index >= hand.size()) {
			return false;
		}
		var selectedCards = gameState.getSelectedCards();
		selectedCards.add(hand.get(index));
		return true;
	}

	public boolean unselect(int index) {
		if (gameState.getPhase() != GamePhase.PLAYING_BLIND) {
			return false;
		}
		var hand = gameState.getCurrentHand();
		if (index >= hand.size()) {
			return false;
		}
		var selectedCards = gameState.getSelectedCards();
		selectedCards.remove(hand.get(index));
		return true;
	}

	public boolean canDiscard() {
		if (gameState.getPhase() != GamePhase.PLAYING_BLIND) {
			return false;
		}
		if (gameState.getCurrentDiscards() < gameState.getMaxDiscards()) {
			return true;
		}
		return false;
	}

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
		draw();
	}

	public boolean canPlay() {
		if (gameState.getPhase() != GamePhase.PLAYING_BLIND) {
			return false;
		}
		if (gameState.getCurrentHandsPlay() < gameState.getMaxHands()) {
			return true;
		}
		return false;
	}

	public void play() {
		if (gameState.getPhase() != GamePhase.PLAYING_BLIND) {
			return;
		}
		var selectedCards = gameState.getSelectedCards();
		var played = HandEvaluator.evaluate(selectedCards);
		var addedScore = HandScorer.score(played, gameState.getHandLevels());
		var stats = gameState.getPlayedHandStats();
		stats.merge(played.type(), 1, Integer::sum);
		var currentScore = gameState.getCurrentBlindScore();
		gameState.setCurrentBlindScore(addedScore.total() + currentScore);
		gameState.getCurrentDeck().discard(selectedCards);
		gameState.getCurrentHand().removeAll(selectedCards);
		selectedCards.clear();
		gameState.setCurrentHandsPlay(gameState.getCurrentHandsPlay() + 1);

		if (isCurrentBlindWon()) {
			if(isGameWon()) {
				winGame();
			}
			winBlind();
		} else if (isCurrentBlindLost()) {
			looseBlind();
		} else {
			draw();
		}
	}

	public boolean isCurrentBlindWon() {
		var currentBlind = gameState.getBlinds().get((gameState.getRound() - 1) % 3);
		var currentBlindScore = gameState.getCurrentBlindScore();
		if (currentBlindScore >= currentBlind.score()) {
			return true;
		}
		return false;
	}

	public boolean isCurrentBlindLost() {
		boolean noHandsLeft = gameState.getCurrentHandsPlay() >= gameState.getMaxHands();
		if (noHandsLeft && !isCurrentBlindWon()) {
			return true;
		}
		return false;
	}

	private void winBlind() {
		var blind = gameState.getBlinds().get((gameState.getRound() - 1) % 3);
		int handBonus = gameState.getMaxHands() - gameState.getCurrentHandsPlay();
		int interest = Math.min(gameState.getDollars() / 5, 5);
		gameState.addDollars(blind.reward() + handBonus + interest);
		enterShop();
	}

	public boolean isGameWon() {
		if (gameState.getAnte() > 8) {
			return true;
		} else if (gameState.getRound() % 3 == 2 && gameState.getAnte() == 8) {
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
		gameState.addDollars(-Shop.PLANET_PRICE);
		gameState.addPlanet(planet);
		shop.remove(offerIndex);
		return true;
	}

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
