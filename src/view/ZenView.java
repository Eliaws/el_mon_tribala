package view;

import controller.GameController;
import controller.PlayResult;
import domain.Blind;
import domain.Card;
import domain.Suit;
import domain.hand.HandType;
import domain.hand.combinations.PlayedHand;
import model.GamePhase;
import model.GameState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.forax.zen.*;
import com.github.forax.zen.PointerEvent.Location;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import java.io.InputStream;

public final class ZenView implements View {

	private boolean isWindowOpen;
	private BufferedImage cardBack;
	private ApplicationContext context;
	private final HashMap<String, Rect> buttons = new HashMap<String, Rect>();

	private final Map<Card, BufferedImage> cardsTextures = new HashMap<Card, BufferedImage>();
	private static final String[] BLIND_LABELS = { "Small Blind", "Big Blind", "Boss Blind" };

	public ZenView() {
		this.isWindowOpen = false;
		try (InputStream input = ZenView.class.getResourceAsStream("/images/back.png")) {
			this.cardBack = ImageIO.read(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void openWindow(GameState state) {

		Application.run(Color.WHITE, context -> {

			this.isWindowOpen = true;
			this.context = context;

			setupButtons(state);
			render(state);
		});

	}

	private void setupButtons(GameState state) {

		int screenWidth = context.getScreenInfo().width();
		int screenHeight = context.getScreenInfo().height();

		// Cartes
		int baseCardHeight = 106;
		int baseCardWidth = 69;

		int cardHeight = screenHeight / 5;
		double ratio = (double) baseCardWidth / baseCardHeight;
		int cardWidth = (int) (cardHeight * ratio);

		int cardSpacing = (int) (cardWidth * 0.8);

		int handSize = state.getHandSize();

		int totalWidth = ((handSize - 1) * cardSpacing) + cardWidth;
		int startX = (screenWidth - totalWidth) / 3 * 2;

		// Boutons Play / Discard
		int buttonWidth = screenWidth / 10;
		int buttonHeight = screenHeight / 15;

		int buttonsTotalWidth = (screenWidth / 10 * 2) + (int) (cardWidth * 1.75);
		int buttonsStartX = startX + (totalWidth - buttonsTotalWidth) / 2;

		int buttonY = screenHeight - cardHeight / 2;

		int buttonSeparation = (int) (screenWidth / 10 + cardWidth * 1.75);

		// Discard
		buttons.put("discard", new Rect(buttonsStartX, buttonY, buttonWidth, buttonHeight));

		// Play
		buttons.put("play", new Rect(buttonsStartX + buttonSeparation, buttonY, buttonWidth, buttonHeight));

		// Sort
		int smallButtonWidth = screenWidth / 20;
		int smallButtonHeight = screenHeight / 30;

		int smallButtonsSpacing = 10;

		int smallButtonsTotalWidth = (smallButtonWidth * 2) + smallButtonsSpacing;
		int smallButtonsStartX = startX + (totalWidth - smallButtonsTotalWidth) / 2;

		int smallButtonY = buttonY + buttonHeight / 2;

		// Rank
		buttons.put("rank", new Rect(smallButtonsStartX, smallButtonY, smallButtonWidth, smallButtonHeight));

		// Suit
		buttons.put("suit", new Rect(smallButtonsStartX + smallButtonWidth + smallButtonsSpacing, smallButtonY,
				smallButtonWidth, smallButtonHeight));

		// Round end
		buttons.put("roundEnd", new Rect((int) ((screenWidth / 3 + screenWidth / 5) - screenWidth / 10),
				screenHeight / 3 + screenHeight / 30, screenWidth / 10 * 2, screenHeight / 10));
	}

	@Override
	public void render(GameState state) {

		if (!isWindowOpen) {
			openWindow(state);
			return;
		}

		this.context.renderFrame(graphics -> {

			graphics.clearRect(0, 0, context.getScreenInfo().width(), context.getScreenInfo().height());
			renderPlaying(state, graphics);
//			switch (state.getPhase()) {
//			case PLAYING_BLIND -> renderPlaying(state, graphics);
//			case SHOP -> renderShop(state, graphics);
//			case GAME_OVER -> renderGameOver(state, graphics);
//			case VICTORY -> renderVictory(state, graphics);
//			}
		});
		return;
	}

	private void renderVictory(GameState state, Graphics2D graphics) {
	}

	private void renderShop(GameState state, Graphics2D graphics) {
	}

	private void renderGameOver(GameState state, Graphics2D graphics) {
	}

	private void renderPlaying(GameState state, Graphics2D graphics) {
		var hand = state.getCurrentHand();
		var selected = state.getSelectedCards();
		renderGame(hand, selected, graphics);
		renderInfo(state, graphics);
	}

	private void renderGame(List<Card> hand, List<Card> selected, Graphics2D graphics) {
		int baseCardHeight = 106;
		int baseCardWidth = 69;

		int screenWidth = context.getScreenInfo().width();
		int screenHeight = context.getScreenInfo().height();
		int cardHeight = screenHeight / 5;
		double ratio = (double) baseCardWidth / baseCardHeight;
		int cardWidth = (int) (cardHeight * ratio);

		int cardSpacing = (int) (cardWidth * 0.8);
		int totalWidth = ((hand.size() - 1) * cardSpacing) + cardWidth;
		int startX = (screenWidth - totalWidth) / 3 * 2;
		int y = screenHeight - cardHeight - screenHeight / 7;

		for (int i = 0; i < hand.size(); i++) {
			BufferedImage image = null;
			Card currentCard = hand.get(i);

			if (!this.cardsTextures.containsKey(currentCard)) {
				String path = "/images/" + formatSuit(currentCard.suit()) + "/" + currentCard.rank() + ".png";
				try {
					InputStream input = ZenView.class.getResourceAsStream(path);
					image = ImageIO.read(input);
					this.cardsTextures.put(currentCard, image);
				} catch (Exception e) {
//					IO.println(path);
//					e.printStackTrace();
				}
			}
			int isSelected = selected.contains(currentCard) == true ? 50 : 0;
			graphics.drawImage(cardsTextures.getOrDefault(currentCard, this.cardBack), startX + (i * cardSpacing),
					y - isSelected, cardWidth, cardHeight, null);
		}

		// button play et discard
		int buttonsTotalWidth = (screenWidth / 10 * 2) + (int) (cardWidth * 1.75);

		graphics.setColor(Color.RED);
		graphics.fillRoundRect(buttons.get("discard").x(), buttons.get("discard").y(), buttons.get("discard").width(),
				buttons.get("discard").height(), 30, 30);
		graphics.setColor(Color.BLUE);
		graphics.fillRoundRect(buttons.get("play").x(), buttons.get("play").y(), buttons.get("play").width(),
				buttons.get("play").height(), 30, 30);
		graphics.setColor(Color.WHITE);
		graphics.setFont(new Font("Arial", Font.BOLD, 16));
		drawCenteredText("Discard", graphics, (buttons.get("discard").x() + buttons.get("discard").width() / 2),
				buttons.get("discard").y() + buttons.get("discard").height() / 2 + 5);
		drawCenteredText("Play", graphics, (buttons.get("play").x() + buttons.get("play").width() / 2),
				buttons.get("play").y() + buttons.get("play").height() / 2 + 5);

		// buttons sort rank/suit

		graphics.setColor(Color.DARK_GRAY);
		graphics.fillRoundRect(buttons.get("rank").x(), buttons.get("rank").y(), buttons.get("rank").width(),
				buttons.get("rank").height(), 20, 20);
		graphics.fillRoundRect(buttons.get("suit").x(), buttons.get("suit").y(), buttons.get("suit").width(),
				buttons.get("suit").height(), 20, 20);
		graphics.setFont(new Font("Arial", Font.PLAIN, 16));
		drawCenteredText("Sort Hand", graphics, buttons.get("discard").x() + buttonsTotalWidth / 2,
				buttons.get("play").y() + buttons.get("rank").height() / 2);
		graphics.setColor(Color.WHITE);
		graphics.setFont(new Font("Arial", Font.PLAIN, 14));
		drawCenteredText("Rank", graphics, buttons.get("rank").x() + buttons.get("rank").width() / 2,
				buttons.get("rank").y() + buttons.get("rank").height() / 2 + 5);
		drawCenteredText("Suit", graphics, buttons.get("suit").x() + buttons.get("suit").width() / 2,
				buttons.get("suit").y() + buttons.get("suit").height() / 2 + 5);

	}

	private void renderInfo(GameState state, Graphics2D graphics) {
		int screenWidth = context.getScreenInfo().width();
		int screenHeight = context.getScreenInfo().height();

		int infoBarStart = screenWidth / 25;
		int infoBarWidth = screenWidth / 5;

		int roundIndex = (state.getRound() - 1) % 3;
		Blind currentBlind = state.getBlinds().get(roundIndex);

		graphics.setColor(Color.DARK_GRAY);
		graphics.fillRect(screenWidth / 25, 0, infoBarWidth, screenHeight);
		graphics.setColor(Color.WHITE);

		// round info
		graphics.setFont(new Font("Arial", Font.PLAIN, 30));
		drawCenteredText(BLIND_LABELS[roundIndex], graphics, infoBarStart + infoBarWidth / 2, screenHeight / 10);
		graphics.setFont(new Font("Arial", Font.PLAIN, 16));
		drawCenteredText("Score at least", graphics, infoBarStart + infoBarWidth / 2, (int) (screenHeight / 20 * 3.5));
		graphics.setFont(new Font("Arial", Font.PLAIN, 30));
		drawCenteredText(String.valueOf(currentBlind.score()), graphics, infoBarStart + infoBarWidth / 2,
				(int) (screenHeight / 20 * 4.5));
		graphics.setFont(new Font("Arial", Font.PLAIN, 16));
		drawCenteredText("Round", graphics, infoBarStart + infoBarWidth / 4, (int) (screenHeight / 20 * 5.75));
		drawCenteredText("Score", graphics, infoBarStart + infoBarWidth / 4, (int) (screenHeight / 20 * 6.25));
		graphics.setFont(new Font("Arial", Font.PLAIN, 22));
		drawCenteredText(String.valueOf(state.getCurrentBlindScore()), graphics,
				(int) (infoBarStart + infoBarWidth / 4 * 2.75), screenHeight / 20 * 6);

		// current Hand
		Optional<PlayedHand> playing;
		if ((playing = state.getPreviewHand()).isPresent()) {
			graphics.setFont(new Font("Arial", Font.PLAIN, 20));
			drawCenteredText(formatHandType(playing.get().type()), graphics, infoBarStart + infoBarWidth / 2,
					(int) (screenHeight / 20 * 8));
		}

		// Hands
		graphics.setFont(new Font("Arial", Font.PLAIN, 22));
		drawCenteredText("Hands", graphics, infoBarStart + infoBarWidth / 4, screenHeight / 20 * 12);
		graphics.setFont(new Font("Arial", Font.PLAIN, 22));
		drawCenteredText(String.valueOf(state.getCurrentHandsPlay()) + " / " + String.valueOf(state.getMaxHands()),
				graphics, infoBarStart + infoBarWidth / 4, screenHeight / 20 * 13);

		// Discard
		graphics.setFont(new Font("Arial", Font.PLAIN, 22));
		drawCenteredText("Discards", graphics, infoBarStart + infoBarWidth / 4 * 3, screenHeight / 20 * 12);
		graphics.setFont(new Font("Arial", Font.PLAIN, 22));
		drawCenteredText(String.valueOf(state.getCurrentDiscards()) + " / " + String.valueOf(state.getMaxDiscards()),
				graphics, infoBarStart + infoBarWidth / 4 * 3, screenHeight / 20 * 13);

		// Game Info
		graphics.setFont(new Font("Arial", Font.PLAIN, 22));
		drawCenteredText("Ante", graphics, infoBarStart + infoBarWidth / 4, screenHeight / 20 * 15);
		graphics.setFont(new Font("Arial", Font.PLAIN, 22));
		drawCenteredText(String.valueOf(state.getAnte()), graphics, infoBarStart + infoBarWidth / 4,
				screenHeight / 20 * 16);

		graphics.setFont(new Font("Arial", Font.PLAIN, 22));
		drawCenteredText("Round", graphics, infoBarStart + infoBarWidth / 4 * 3, screenHeight / 20 * 15);
		graphics.setFont(new Font("Arial", Font.PLAIN, 22));
		drawCenteredText(String.valueOf(state.getRound()), graphics, infoBarStart + infoBarWidth / 4 * 3,
				screenHeight / 20 * 16);
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

	private void drawCenteredText(String text, Graphics2D graphics, int x, int y) {
		FontMetrics metrics = graphics.getFontMetrics();
		int textWidth = metrics.stringWidth(text);
		graphics.drawString(text, x - textWidth / 2, y);

	}

	private String formatSuit(Suit s) {
		String out = "";
		switch (s) {
		case Clovers -> out = "space";
		case Diamonds -> out = "death";
		case Hearts -> out = "life";
		case Spades -> out = "time";
		}
		return out;
	}

	@Override
	public void renderHelp() {
		// TODO Auto-generated method stub

	}

	@Override
	public void renderInvalidInput(GameState state, String message) {
		// TODO Auto-generated method stub

	}

	private void renderPlayResult(GameState state, PlayResult result) {
		context.renderFrame(graphics -> {
			graphics.clearRect(0, 0, context.getScreenInfo().width(), context.getScreenInfo().height());
			renderInfo(state, graphics);
			renderRoundEnd(state, graphics);
		});

	}

	private void renderRoundEnd(GameState state, Graphics2D graphics) {
		int screenWidth = context.getScreenInfo().width();
		int screenHeight = context.getScreenInfo().height();

		graphics.setColor(Color.DARK_GRAY);
		graphics.fillRoundRect(screenWidth / 3, screenHeight / 3, screenWidth / 5 * 2, screenHeight / 3, 30, 30);

		graphics.setColor(Color.ORANGE);
		graphics.fillRoundRect(buttons.get("roundEnd").x(), buttons.get("roundEnd").y(),
				buttons.get("roundEnd").width(), buttons.get("roundEnd").height(), 30, 30);
		graphics.setColor(Color.WHITE);
		graphics.setFont(new Font("Arial", Font.BOLD, 30));
		int roundIndex = (state.getRound() - 1) % 3;
		int remainingHands = state.getMaxHands() - state.getCurrentHandsPlay();
		drawCenteredText("Cash Out: $" + String.valueOf(state.getBlinds().get(roundIndex).reward() + remainingHands),
				graphics, buttons.get("roundEnd").x() + buttons.get("roundEnd").width() / 2,
				buttons.get("roundEnd").y() + buttons.get("roundEnd").height() / 2 + 10);
		graphics.setFont(new Font("Arial", Font.PLAIN, 16));
		graphics.setColor(Color.WHITE);
		graphics.drawString(BLIND_LABELS[roundIndex], screenWidth / 3 + screenWidth / 20, screenHeight / 32 * 17);
		graphics.drawString(String.valueOf(remainingHands) + " Remaining Hands [$1 each]",
				screenWidth / 3 + screenWidth / 20, screenHeight / 32 * 19);
		graphics.setColor(Color.ORANGE);
		graphics.drawString("$".repeat(state.getBlinds().get(roundIndex).reward()),
				screenWidth / 3 * 2 - screenWidth / 20, screenHeight / 32 * 17);
		graphics.drawString("$".repeat(remainingHands), screenWidth / 3 * 2 - screenWidth / 20, screenHeight / 32 * 19);
	}

	@Override
	public List<String> getUserInput(GameState state) {

		List<String> out = new ArrayList<>();

		while (true) {
			Event event = context.pollOrWaitEvent(10); // 60 fps a peu pres (j'ai check a la main)
			if (event == null) {
				return null;
			}
			switch (event) {
			case PointerEvent pointerEvent -> {
				if (pointerEvent.action() == PointerEvent.Action.POINTER_DOWN) {
					String input = detectClick(state, pointerEvent.location());
					if (input != null) {
						if (input.equals("")) {
							return out;
						}
						out.add(input);
						return out;
					}
				}
			}
			default -> {
			}
			}
		}
	}

	private String detectClick(GameState state, Location mouseClick) {
		switch (state.getPhase()) {
		case GamePhase.PLAYING_BLIND -> {
			if (state.getBlinds().get((state.getRound() - 1) % 3).score() < state.getCurrentBlindScore()) {
				if (isButtonClick(mouseClick, state.getHandSize()) == 4) {
					return "";
				}
			}
			int cardIndex = isClickCard(mouseClick, state.getHandSize());
			if (cardIndex > -1) {
				return String.valueOf(cardIndex);
			} else {
				int button = isButtonClick(mouseClick, state.getHandSize());
				switch (button) {
				case 0:
					return "play";
				case 1:
					return "discard";
				case 2:
					return "r";
				case 3:
					return "s";
				}
			}
		}
		case GamePhase.FINISHED_BLIND -> {

		}
		}
		return null;
	}

	private int isButtonClick(Location mouseClick, int handSize) {
		if (buttons.get("play").isClicked(mouseClick)) {
			return 0;
		}
		if (buttons.get("discard").isClicked(mouseClick)) {
			return 1;
		}
		if (buttons.get("rank").isClicked(mouseClick)) {
			return 2;
		}
		if (buttons.get("suit").isClicked(mouseClick)) {
			return 3;
		}
		if (buttons.get("roundEnd").isClicked(mouseClick)) {
			return 4;
		}

		return -1;
	}

	private int isClickCard(Location mouseClick, int handSize) {
		int baseCardHeight = 106;
		int baseCardWidth = 69;
		int screenWidth = context.getScreenInfo().width();
		int screenHeight = context.getScreenInfo().height();
		int cardHeight = screenHeight / 5;
		double ratio = (double) baseCardWidth / baseCardHeight;
		int cardWidth = (int) (cardHeight * ratio);

		int cardSpacing = (int) (cardWidth * 0.8);
		int totalWidth = ((handSize - 1) * cardSpacing) + cardWidth;
		int startX = (screenWidth - totalWidth) / 3 * 2;
		int y = screenHeight - cardHeight - screenHeight / 7;

		if (mouseClick.x() > startX && mouseClick.x() < startX + cardSpacing * handSize && mouseClick.y() > y
				&& mouseClick.y() < y + cardHeight) {
			int cardIndex = (mouseClick.x() - startX) / cardSpacing;
			return cardIndex;
		}

		return -1;
	}

//	public static void main(String[] args) {
//
//		ZenView test = new ZenView();
//		GameController gc = new GameController(test);
//		gc.start();
//		test.openWindow(gc.getGameState());
//		while (true) {
//			List<String> action;
//			while (!(action = test.getUserInput(gc.getGameState())).isEmpty()) {
	//// int index = Integer.parseInt(action.get(0));
//				IO.println(action);
//
	////				gc.toggle(index);
//				test.render(gc.getGameState());
//			}
//
//		}
//	}

}
