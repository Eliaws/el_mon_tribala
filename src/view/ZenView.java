package view;

import controller.PlayResult;
import domain.Card;
import domain.Suit;
import model.GameState;

import java.util.List;

import com.github.forax.zen.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import java.io.InputStream;

public final class ZenView implements View {

	private boolean isWindowOpen;
	private ApplicationContext context;

	public ZenView() {
		this.isWindowOpen = false;
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
		for (int i = 0; i < hand.size(); i++) {
			BufferedImage image = null;
			Card currentCard = hand.get(i);
			formatSuit(currentCard.suit());
			// "/images/"+ formatSuit(hand.get(i).suit())+"/" + currentCard.toString() + ".png"
			try (InputStream input = ZenView.class.getResourceAsStream("/images/back.png")) {
				image = ImageIO.read(input);
			} catch (Exception e) {
				e.printStackTrace();
			}
			BufferedImage finalImage = image;
			
			graphics.drawImage(finalImage, 110 * i, 0, 103, 159, null);
		}
	}

	private String formatSuit(Suit s) {
		String out = "";
		switch (s) {
		case Clovers:
			out = "space";
		case Diamonds:
			out = "death";
		case Hearts:
			out = "life";
		case Spades:
			out = "time";
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
		test.openWindow(null);
	}

}
