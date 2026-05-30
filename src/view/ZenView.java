package view;

import controller.GameController;
import controller.PlayResult;
import domain.Card;
import domain.Suit;
import model.GameState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.forax.zen.*;

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

	@Override
	public void render(GameState state) {

		if (!isWindowOpen) {
			openWindow(state);
			return;
		}

		context.renderFrame(graphics -> {

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
		renderCards(hand, selected, graphics);
		renderInfo(state, graphics);
	}
	
	private void renderCards(List<Card> hand, List<Card> selected, Graphics2D graphics) {
		int baseCardHeight = 106;
		int baseCardWidth = 69;
		
		int screenWidth = context.getScreenInfo().width();
		int screenHeight = context.getScreenInfo().height();
		int cardHeight = screenHeight/5;
		double ratio = (double) baseCardWidth / baseCardHeight;
		int cardWidth = (int)(cardHeight * ratio);
		
		int cardSpacing = (int) (cardWidth * 0.8);
		int totalWidth = hand.size() * cardSpacing;
		int startX = (screenWidth - totalWidth) / 2;
		int y = screenHeight - cardHeight - 40;

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
					IO.println(path);
					e.printStackTrace();
				}
			}
			int isSelected = selected.contains(currentCard) == true ? 50 : 0;
			graphics.drawImage(
					cardsTextures.getOrDefault(currentCard, this.cardBack), 
					startX + (i * cardSpacing), y - isSelected, cardWidth, cardHeight, null);
		}
	}
	
	private void renderInfo(GameState state, Graphics2D graphics) {
		int screenWidth = context.getScreenInfo().width();
		int screenHeight = context.getScreenInfo().height();

		int infoBarStart = screenWidth/25;
		int infoBarWidth = screenWidth/5;
		
        graphics.setColor(Color.BLACK);
		graphics.fillRect(screenWidth/25, 0, infoBarWidth, screenHeight);
        graphics.setColor(Color.WHITE);

        graphics.setFont(new Font("Arial", Font.PLAIN, 16));
        drawCenteredText("CECI EST UN TEST", graphics, infoBarStart+infoBarWidth/2, screenHeight/10);
	}
	
	private void drawCenteredText(String text, Graphics2D graphics, int x, int y) {
		FontMetrics metrics = graphics.getFontMetrics();
		int textWidth = metrics.stringWidth(text);
		graphics.drawString(text, x-textWidth/2, y);
		
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

	@Override
	public void renderPlayResult(GameState state, PlayResult result) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<String> getUserInput(GameState state) {
		// TODO Auto-generated method stub
		return null;
	}

	private void openWindow(GameState state) {

		Application.run(Color.WHITE, context -> {

			this.isWindowOpen = true;
			this.context = context;

			render(state);
		});

	}

	public static void main(String[] args) {

		ZenView test = new ZenView();
		GameController gc = new GameController(test);
		gc.start();
		gc.select(0);
		test.openWindow(gc.getGameState());
	}

}
