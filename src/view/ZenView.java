package view;

import controller.PlayResult;
import domain.Blind;
import domain.Card;
import domain.Shop;
import domain.Suit;
import domain.consummables.Planet;
import domain.hand.HandType;
import domain.hand.combinations.PlayedHand;
import model.GamePhase;
import model.GameState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import com.github.forax.zen.*;
import com.github.forax.zen.PointerEvent.Location;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import java.io.InputStream;

public final class ZenView implements View {

	private boolean isWindowOpen;
	private BufferedImage cardBack;
	private ApplicationContext context;
	private Font font;
	private final HashMap<String, Rect> buttons = new HashMap<String, Rect>();
	private final Map<Card, BufferedImage> cardsTextures = new HashMap<Card, BufferedImage>();
	private final Map<Planet, BufferedImage> planetsTextures = new HashMap<Planet, BufferedImage>();
	private static final String[] BLIND_LABELS = { "Small Blind", "Big Blind", "Boss Blind" };

	// Palette des écrans de fin (Game Over / Victory)
	private static final Color END_OVERLAY = new Color(0, 0, 0, 180);
	private static final Color END_PANEL = new Color(16, 18, 28);
	private static final Color END_LOSE = new Color(214, 60, 60);
	private static final Color END_WIN = new Color(63, 185, 80);
	private static final Color END_GOLD = new Color(241, 175, 60);

	// Conteneurs (fonds) des éléments de la barre d'info latérale
	private static final Color INFO_PANEL = new Color(18, 20, 30);
	private static final Color INFO_PANEL_BORDER = new Color(60, 64, 80);

	// Toast « main jouée » affiché en haut de la zone de jeu pendant la partie
	private static final long TOAST_DURATION_MS = 4000;
	private static final long TOAST_FADE_MS = 700;
	private PlayResult lastShownResult;
	private long resultShownAt;
	private final ArrayList<ArrayList<Integer[]>> starsLayers = new ArrayList<ArrayList<Integer[]>>();
	private Point mouseCoords;

	public ZenView() {
		this.isWindowOpen = false;
		try (InputStream input = ZenView.class.getResourceAsStream("/ressources/images/back.png")) {
			this.cardBack = ImageIO.read(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			this.font = Font.createFont(Font.TRUETYPE_FONT,
					ZenView.class.getResourceAsStream("/ressources/fonts/Letterstyle.ttf"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void openWindow(GameState state) {

		Application.run(Color.BLACK, context -> {

			this.isWindowOpen = true;
			this.context = context;

			setupButtons(state);
			render(state);
		});
	}

	public void closeWindow() {
		this.context.dispose();
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

		// buy planets
		int planetHeight = (int) (screenHeight / 4.5);
		int planetWidth = (int) (planetHeight * ratio);

		int planetSpacing = (int) (planetWidth * 1.1);
		int planetsStartX = screenWidth / 3 + screenHeight / 50;
		int y = screenHeight / 3 + screenHeight / 50;

		for (int i = 0; i < Shop.SLOT_COUNT; i++) {
			buttons.put("planet" + String.valueOf(i),
					new Rect(planetsStartX + (i * planetSpacing), y, planetWidth, planetHeight));
		}

		// close shop
		buttons.put("exitShop",
				new Rect(screenWidth / 3 + buttons.get("planet2").width() * 3 + screenHeight / 50 * 3,
						buttons.get("planet2").y(),
						(screenWidth / 5 * 2) - buttons.get("planet2").width() * 3 - screenHeight / 50 * 4,
						buttons.get("planet2").height() / 2 - screenHeight / 50));

		// Play Again (Game Over / Victory)
		int endPanelHeight = screenHeight * 2 / 3;
		int endPanelY = (screenHeight - endPanelHeight) / 2;
		int playAgainWidth = screenWidth / 5;
		int playAgainHeight = screenHeight / 12;
		buttons.put("playAgain", new Rect((screenWidth - playAgainWidth) / 2,
				endPanelY + endPanelHeight - playAgainHeight - screenHeight / 30, playAgainWidth, playAgainHeight));
		
		// close button
		int quitRadius = screenWidth / 40;
		buttons.put("quit", new Rect((int) (screenWidth-(quitRadius*1.5)), quitRadius/2, quitRadius, quitRadius));
	}

	@Override
	public void render(GameState state) {

		if (!isWindowOpen) {
			openWindow(state);
			return;
		}

		this.context.renderFrame(graphics -> {

			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

			graphics.clearRect(0, 0, context.getScreenInfo().width(), context.getScreenInfo().height());
			drawBackground(graphics);
			switch (state.getPhase()) {
			case PLAYING_BLIND -> renderPlaying(state, graphics);
			case FINISHED_BLIND -> renderPlayResult(state, graphics);
			case SHOP -> renderShop(state, graphics);
			case GAME_OVER -> renderGameOver(state, graphics);
			case VICTORY -> renderVictory(state, graphics);
			}
			drawQuitButton(graphics);
		});
		return;
	}

	private void drawQuitButton(Graphics2D graphics) {
		int y = buttons.get("quit").y();
		int x = buttons.get("quit").x();
		int w = buttons.get("quit").width();
		graphics.setColor(Color.DARK_GRAY);
		graphics.fillOval(x, y, w, w);
		graphics.setColor(Color.WHITE);
		x += w/2;
		y+= w/2;
		w /=5;
		graphics.drawLine(x-w, y-w, x+w, y+w);
		graphics.drawLine(x+w, y-w, x-w, y+w);
		
	}

	private void drawBackground(Graphics2D graphics) {
		int screenWidth = context.getScreenInfo().width();
		int screenHeight = context.getScreenInfo().height();
		var gradient = new GradientPaint(0, 0, Color.BLACK, 0, screenHeight, new Color(25, 0, 50));
		graphics.setPaint(gradient);
		graphics.fillRect(0, 0, screenWidth, screenHeight);
		Random rand = new Random();
		if (starsLayers.isEmpty()) {
			for (int j = 0; j < 3; j++) {
				ArrayList<Integer[]> layer = new ArrayList<Integer[]>();
				for (int i = 0; i < 500; i++) {
					Integer[] coords = { rand.nextInt(-50, screenWidth + 50), rand.nextInt(-50, screenHeight + 50),
							rand.nextInt(3) };
					// star[0] = x coord, star[1] = y coord, star[2] type (x or crosss or circle) 
					layer.add(coords);
				}
				starsLayers.add(layer);
			}
		}
		int layer = 1;
		for (ArrayList<Integer[]> stars : this.starsLayers) {
			for (Integer[] star : stars) {
				drawStar(star, layer, graphics);
			}
			layer++;
		}
	}

	private void drawStar(Integer[] star, int layer, Graphics2D graphics) {
		int layerVariance = 0;
		switch (layer) {
		case 3 -> {
			graphics.setColor(Color.WHITE);
			layerVariance = (int) (layer * 1.15);
		}
		case 2 -> {
			graphics.setColor(Color.GRAY);
			layerVariance = (int) (layer * 1.09);
		}
		case 1 -> {
			graphics.setColor(Color.DARK_GRAY);
			layerVariance = (int) (layer * 1.05);
		}
		}
		int xModif = 0;
		int yModif = 0;
		if (this.mouseCoords != null) {
			xModif = (int) (this.mouseCoords.getX() * (0.01 * layerVariance));
			yModif = (int) (this.mouseCoords.getY() * (0.01 * layerVariance));
		}
		int size = 2;
		switch (star[2]) {
		case 0 -> {
			graphics.fillOval(star[0] + xModif, star[1] + yModif, size * 2, size * 2);
		}
		case 1 -> {
			graphics.drawLine(star[0] - size + xModif, star[1] - size + yModif, star[0] + size + xModif,
					star[1] + size + yModif);
			graphics.drawLine(star[0] + size + xModif, star[1] - size + yModif, star[0] - size + xModif,
					star[1] + size + yModif);

		}
		default -> {
			graphics.drawLine(star[0] + xModif, star[1] - size + yModif, star[0] + xModif, star[1] + size + yModif);
			graphics.drawLine(star[0] - size + xModif, star[1] + yModif, star[0] + size + xModif, star[1] + yModif);
		}
		}
	}

	private void renderVictory(GameState state, Graphics2D graphics) {
		renderPlaying(state, graphics);
		List<String> lines = List.of("Round final : " + state.getRound(), "Cash : $" + state.getDollars());
		renderEndScreen(graphics, "YOU WIN!", END_WIN, "Tu as conquis les 8 Antes", lines, END_WIN);
	}

	/**
	 * Dessine un écran de fin générique (panneau centré, titre, sous-titre, lignes
	 * de stats et bouton « Play Again »). Ne fait que de la présentation : toutes
	 * les valeurs sont lues depuis l'état.
	 */
	private void renderEndScreen(Graphics2D graphics, String title, Color accent, String subtitle, List<String> lines,
			Color buttonColor) {
		int screenWidth = context.getScreenInfo().width();
		int screenHeight = context.getScreenInfo().height();

		// voile sombre translucide par-dessus le plateau de jeu (toujours visible)
		graphics.setColor(END_OVERLAY);
		graphics.fillRect(0, 0, screenWidth, screenHeight);

		// panneau central
		int panelWidth = screenWidth / 2;
		int panelHeight = screenHeight * 2 / 3;
		int panelX = (screenWidth - panelWidth) / 2;
		int panelY = (screenHeight - panelHeight) / 2;
		graphics.setColor(END_PANEL);
		graphics.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 40, 40);
		graphics.setColor(accent);
		graphics.setStroke(new BasicStroke(4));
		graphics.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 40, 40);

		// titre (police thématique)
		graphics.setColor(accent);
		graphics.setFont(getFont(screenHeight / 12f));
		drawCenteredText(title, graphics, screenWidth / 2, panelY + panelHeight / 4);

		// sous-titre
		graphics.setColor(Color.WHITE);
		graphics.setFont(getFont(screenHeight / 36));
		drawCenteredText(subtitle, graphics, screenWidth / 2, panelY + panelHeight / 4 + screenHeight / 16);

		// lignes de stats
		graphics.setFont(getFont(screenHeight / 40));
		int lineY = screenHeight / 2;
		for (String line : lines) {
			drawCenteredText(line, graphics, screenWidth / 2, lineY);
			lineY += screenHeight / 22;
		}

		// bouton Play Again
		Rect button = buttons.get("playAgain");
		graphics.setColor(buttonColor);
		graphics.fillRoundRect(button.x(), button.y(), button.width(), button.height(), 25, 25);
		graphics.setColor(Color.WHITE);
		graphics.setFont(getFont(screenHeight / 36));
		drawCenteredText("Rejouer", graphics, button.x() + button.width() / 2,
				button.y() + button.height() / 2 + screenHeight / 90);
	}

	private void renderShop(GameState state, Graphics2D graphics) {
		renderInfo(state, graphics);

		int screenWidth = context.getScreenInfo().width();
		int screenHeight = context.getScreenInfo().height();

		graphics.setColor(Color.DARK_GRAY);
		graphics.fillRoundRect(screenWidth / 3, screenHeight / 3, screenWidth / 5 * 2, screenHeight, 30, 30);

		renderPlanets(state, graphics);

		graphics.setColor(Color.RED);
		graphics.fillRoundRect(buttons.get("exitShop").x(), buttons.get("exitShop").y(),
				buttons.get("exitShop").width(), buttons.get("exitShop").height(), 30, 30);
		graphics.setColor(Color.WHITE);
		graphics.setFont(getFont(16));
		drawCenteredText("Next Round", graphics, buttons.get("exitShop").x() + buttons.get("exitShop").width() / 2,
				buttons.get("exitShop").y() + buttons.get("exitShop").height() / 2 + 5);

	}

	private void renderPlanets(GameState state, Graphics2D graphics) {
		List<Planet> planets = state.getShop().getOffers();
		// planets
		for (int i = 0; i < planets.size(); i++) {
			BufferedImage image = null;
			Planet currentPlanet = planets.get(i);

			if (!this.planetsTextures.containsKey(currentPlanet)) {
				String path = "/ressources/images/planets/" + currentPlanet.name().toLowerCase() + ".png";
				try {
					InputStream input = ZenView.class.getResourceAsStream(path);
					image = ImageIO.read(input);
					this.planetsTextures.put(currentPlanet, image);
				} catch (Exception e) {
					IO.println(path);
					e.printStackTrace();
				}
			}
			graphics.drawImage(planetsTextures.getOrDefault(currentPlanet, this.cardBack),
					buttons.get("planet" + String.valueOf(i)).x(), buttons.get("planet" + String.valueOf(i)).y(),
					buttons.get("planet" + String.valueOf(i)).width(),
					buttons.get("planet" + String.valueOf(i)).height(), null);
			graphics.setColor(Color.ORANGE);
			graphics.setFont(getFont(16));
			drawCenteredText("$" + String.valueOf(Shop.PLANET_PRICE), graphics,
					buttons.get("planet" + String.valueOf(i)).x()
							+ buttons.get("planet" + String.valueOf(i)).width() / 2,
					(int) (buttons.get("planet" + String.valueOf(i)).y()
							+ buttons.get("planet" + String.valueOf(i)).height() * 1.1));

		}

	}

	private void renderGameOver(GameState state, Graphics2D graphics) {
		renderPlaying(state, graphics);
		int roundIndex = (state.getRound() - 1) % 3;
		Blind currentBlind = state.getBlinds().get(roundIndex);
		List<String> lines = List.of("Score : " + state.getCurrentBlindScore() + " / " + currentBlind.score(),
				"Round atteint : " + state.getRound(), "Cash : $" + state.getDollars());
		renderEndScreen(graphics, "GAME OVER", END_LOSE,
				"Battu à l'Ante " + state.getAnte() + " - " + BLIND_LABELS[roundIndex], lines, END_GOLD);
	}

	private void renderPlaying(GameState state, Graphics2D graphics) {
		var hand = state.getCurrentHand();
		var selected = state.getSelectedCards();
		renderGame(hand, selected, graphics);
		renderInfo(state, graphics);
		renderPlayedHandToast(state, graphics);
	}

	/**
	 * Bandeau temporaire affiché en haut de la zone de jeu après une main jouée :
	 * type de main + calcul {@code chips × mult = total}. Apparaît à chaque play,
	 * se met à jour à la main suivante et disparaît en fondu après quelques
	 * secondes. La donnée provient de {@code state.getLastResult()} ; seul le
	 * timing d'affichage (présentation) est géré ici.
	 */
	private void renderPlayedHandToast(GameState state, Graphics2D graphics) {
		if (state.getPhase() != GamePhase.PLAYING_BLIND) {
			return;
		}
		PlayResult result = state.getLastResult();
		if (result == null) {
			return;
		}
		// début de blinde : aucune main jouée ce tour → on consomme un éventuel
		// résultat résiduel (main gagnante de la blinde précédente) sans l'afficher
		if (state.getCurrentHandsPlay() == 0) {
			lastShownResult = result;
			return;
		}
		long now = System.currentTimeMillis();
		if (result != lastShownResult) {
			lastShownResult = result;
			resultShownAt = now;
		}
		long elapsed = now - resultShownAt;
		if (elapsed > TOAST_DURATION_MS) {
			return;
		}
		// fondu sur la fin de l'affichage
		int alpha = 255;
		if (elapsed > TOAST_DURATION_MS - TOAST_FADE_MS) {
			alpha = (int) (255 * (TOAST_DURATION_MS - elapsed) / TOAST_FADE_MS);
		}
		alpha = Math.max(0, Math.min(255, alpha));

		int screenWidth = context.getScreenInfo().width();
		int screenHeight = context.getScreenInfo().height();

		// centré horizontalement sur la zone des cartes (2/3 droite de l'écran)
		int cardHeight = screenHeight / 5;
		double ratio = 69.0 / 106.0;
		int cardWidth = (int) (cardHeight * ratio);
		int cardSpacing = (int) (cardWidth * 0.8);
		int totalWidth = ((state.getHandSize() - 1) * cardSpacing) + cardWidth;
		int startX = (screenWidth - totalWidth) / 3 * 2;
		int centerX = startX + totalWidth / 2;

		int panelWidth = (int) (totalWidth * 0.7);
		int panelHeight = screenHeight / 9;
		int panelX = centerX - panelWidth / 2;
		int panelY = screenHeight / 40;

		graphics.setColor(new Color(16, 18, 28, alpha * 200 / 255));
		graphics.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 24, 24);
		graphics.setColor(new Color(241, 175, 60, alpha));
		graphics.setStroke(new BasicStroke(3));
		graphics.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 24, 24);

		// type de main
		graphics.setColor(new Color(241, 175, 60, alpha));
		graphics.setFont(getFont(panelHeight / 4));
		drawCenteredText(formatHandType(result.hand().type()), graphics, centerX, panelY + panelHeight * 2 / 5);

		// calcul chips × mult = total
		graphics.setFont(getFont(panelHeight / 5));
		drawScoreFormula(graphics, centerX, panelY + panelHeight * 4 / 5, result.score().chips(), result.score().mult(),
				result.score().total(), alpha);
	}

	/**
	 * Dessine, centré sur {@code centerX}, la formule de score « chips × mult =
	 * total » avec les chips en bleu, le mult en rouge et le total en vert.
	 */
	private void drawScoreFormula(Graphics2D graphics, int centerX, int y, int chips, int mult, int total, int alpha) {
		String sChips = String.valueOf(chips);
		String sMult = String.valueOf(mult);
		String sTotal = String.valueOf(total);
		String times = "  ×  ";
		String equals = "  =  ";
		FontMetrics metrics = graphics.getFontMetrics();
		int fullWidth = metrics.stringWidth(sChips + times + sMult + equals + sTotal);
		int x = centerX - fullWidth / 2;
		Color neutral = new Color(225, 225, 225, alpha);

		graphics.setColor(new Color(80, 140, 255, alpha));
		graphics.drawString(sChips, x, y);
		x += metrics.stringWidth(sChips);
		graphics.setColor(neutral);
		graphics.drawString(times, x, y);
		x += metrics.stringWidth(times);
		graphics.setColor(new Color(230, 80, 80, alpha));
		graphics.drawString(sMult, x, y);
		x += metrics.stringWidth(sMult);
		graphics.setColor(neutral);
		graphics.drawString(equals, x, y);
		x += metrics.stringWidth(equals);
		graphics.setColor(new Color(80, 200, 110, alpha));
		graphics.drawString(sTotal, x, y);
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
				String path = "/ressources/images/" + formatSuit(currentCard.suit()) + "/" + currentCard.rank()
						+ ".png";
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
		graphics.setFont(getFont(16));
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
		graphics.setFont(getFont(16));
		graphics.setColor(Color.LIGHT_GRAY);
		drawCenteredText("Sort Hand", graphics, buttons.get("discard").x() + buttonsTotalWidth / 2,
				buttons.get("play").y() + buttons.get("rank").height() / 2);
		graphics.setColor(Color.WHITE);
		graphics.setFont(getFont(14));
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

		graphics.setColor(new Color(70, 70, 70, 200));
		graphics.fillRect(screenWidth / 25, 0, infoBarWidth, screenHeight);
		graphics.setColor(Color.WHITE);

		// round info
		GamePhase currentPhase = state.getPhase();
		if (currentPhase == GamePhase.PLAYING_BLIND || currentPhase == GamePhase.FINISHED_BLIND) {
			graphics.setFont(getFont(30));
			drawCenteredText(BLIND_LABELS[roundIndex], graphics, infoBarStart + infoBarWidth / 2, screenHeight / 10);
			graphics.setFont(getFont(16));
			drawCenteredText("Score at least", graphics, infoBarStart + infoBarWidth / 2,
					(int) (screenHeight / 20 * 3.5));
			graphics.setColor(Color.RED);
			graphics.setFont(getFont(28));
			drawCenteredText(String.valueOf(currentBlind.score()), graphics, infoBarStart + infoBarWidth / 2,
					(int) (screenHeight / 20 * 4.5));
			graphics.setColor(Color.WHITE);
			graphics.setFont(getFont(16));
			drawCenteredText("Round", graphics, infoBarStart + infoBarWidth / 4, (int) (screenHeight / 20 * 5.75));
			drawCenteredText("Score", graphics, infoBarStart + infoBarWidth / 4, (int) (screenHeight / 20 * 6.25));
			graphics.setFont(getFont(22));
			drawCenteredText(String.valueOf(state.getCurrentBlindScore()), graphics,
					(int) (infoBarStart + infoBarWidth / 4 * 2.75), screenHeight / 20 * 6);
		}
		if (currentPhase == GamePhase.SHOP) {
			graphics.setFont(getFont(35));
			drawCenteredText("SHOP", graphics, infoBarStart + infoBarWidth / 2, screenHeight / 6);
			graphics.setFont(getFont(16));
			drawCenteredText("Improve your run!", graphics, infoBarStart + infoBarWidth / 2, screenHeight / 6 * 2);
		}

		// current Hand
		// mult + chips
		int spacing = screenWidth / 100;
		int startX = screenWidth / 25 + spacing;
		int width = (screenWidth / 5 - screenWidth / 25) / 2;

		graphics.setColor(Color.BLUE);
		graphics.fillRoundRect(startX, screenHeight / 20 * 9, width, screenHeight / 15, 30, 30);
		graphics.setColor(Color.RED);
		graphics.setFont(getFont(20));
		graphics.fillRoundRect(startX + spacing * 2 + width, screenHeight / 20 * 9, width, screenHeight / 15, 30, 30);
		drawCenteredText("X", graphics, infoBarStart + infoBarWidth / 2,
				screenHeight / 20 * 9 + screenHeight / 15 / 2 + 10);

		// level
		Optional<PlayedHand> playing;
		graphics.setColor(Color.WHITE);
		if ((playing = state.getPreviewHand()).isPresent() && state.getPreviewScore().isPresent()) {
			PlayedHand hand = playing.get();
			int level = state.getHandLevels().getOrDefault(playing.get().type(), 0) + 1;
			graphics.setFont(getFont(22));
			drawCenteredText(formatHandType(playing.get().type()) + " lvl." + String.valueOf(level), graphics,
					infoBarStart + infoBarWidth / 2, (int) (screenHeight / 20 * 8));
			drawCenteredText(String.valueOf(hand.type().baseChips() + ((level - 1) * hand.type().levelChips())),
					graphics, startX + screenHeight / 15, screenHeight / 20 * 9 + screenHeight / 30 + 10);
			drawCenteredText(String.valueOf(hand.type().baseMult() + ((level - 1) * hand.type().levelMult())), graphics,
					startX + spacing * 2 + width + screenHeight / 15, screenHeight / 20 * 9 + screenHeight / 30 + 10);

		}

		// Conteneurs (fonds) autour de Hands / Discards / Ante / Round.
		// Boxes centrées horizontalement sur le centre du texte (infoBarWidth/4 et
		// /4*3) et verticalement sur le bloc libellé + valeur via les métriques.
		int unit = screenHeight / 20;
		int colMargin = infoBarWidth / 20;
		int boxWidth = infoBarWidth / 2 - colMargin;
		int leftBoxX = (infoBarStart + infoBarWidth / 4) - boxWidth / 2;
		int rightBoxX = (infoBarStart + infoBarWidth / 4 * 3) - boxWidth / 2;

		graphics.setFont(getFont(22));
		FontMetrics infoMetrics = graphics.getFontMetrics();
		int pad = unit / 4;
		int boxHeight = infoMetrics.getAscent() + unit + infoMetrics.getDescent() + pad * 2;
		int handsBoxY = screenHeight / 20 * 12 - infoMetrics.getAscent() - pad;
		int anteBoxY = screenHeight / 20 * 15 - infoMetrics.getAscent() - pad;

		drawInfoContainer(graphics, leftBoxX, handsBoxY, boxWidth, boxHeight); // Hands
		drawInfoContainer(graphics, rightBoxX, handsBoxY, boxWidth, boxHeight); // Discards
		drawInfoContainer(graphics, leftBoxX, anteBoxY, boxWidth, boxHeight); // Ante
		drawInfoContainer(graphics, rightBoxX, anteBoxY, boxWidth, boxHeight); // Round

		// Hands
		graphics.setColor(Color.WHITE);
		graphics.setFont(getFont(20));
		drawCenteredText("Hands", graphics, infoBarStart + infoBarWidth / 4, screenHeight / 20 * 12);
		graphics.setFont(getFont(22));
		graphics.setColor(Color.BLUE);
		drawCenteredText(String.valueOf(state.getCurrentHandsPlay()) + " / " + String.valueOf(state.getMaxHands()),
				graphics, infoBarStart + infoBarWidth / 4, screenHeight / 20 * 13);

		// Discard
		graphics.setColor(Color.WHITE);
		graphics.setFont(getFont(20));
		drawCenteredText("Discards", graphics, infoBarStart + infoBarWidth / 4 * 3, screenHeight / 20 * 12);
		graphics.setFont(getFont(22));
		graphics.setColor(Color.RED);
		drawCenteredText(String.valueOf(state.getCurrentDiscards()) + " / " + String.valueOf(state.getMaxDiscards()),
				graphics, infoBarStart + infoBarWidth / 4 * 3, screenHeight / 20 * 13);

		// Game Info
		graphics.setColor(Color.WHITE);
		graphics.setFont(getFont(20));
		drawCenteredText("Ante", graphics, infoBarStart + infoBarWidth / 4, screenHeight / 20 * 15);
		drawCenteredText("Round", graphics, infoBarStart + infoBarWidth / 4 * 3, screenHeight / 20 * 15);

		graphics.setColor(Color.ORANGE);
		graphics.setFont(getFont(22));
		drawCenteredText(String.valueOf(state.getAnte()), graphics, infoBarStart + infoBarWidth / 4,
				screenHeight / 20 * 16);
		drawCenteredText(String.valueOf(state.getRound()), graphics, infoBarStart + infoBarWidth / 4 * 3,
				screenHeight / 20 * 16);

		drawCenteredText("$" + String.valueOf(state.getDollars()), graphics, infoBarStart + infoBarWidth / 2,
				screenHeight / 20 * 18);
	}

	@Override
	public void renderHelp() {
		// TODO Auto-generated method stub

	}

	private void renderPlayResult(GameState state, Graphics2D graphics) {
		renderInfo(state, graphics);
		renderRoundEnd(state, graphics);
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
		graphics.setFont(getFont(28));
		int roundIndex = (state.getRound() - 1) % 3;
		int remainingHands = state.getMaxHands() - state.getCurrentHandsPlay();
		drawCenteredText("Cash Out: $" + String.valueOf(state.getBlinds().get(roundIndex).reward() + remainingHands),
				graphics, buttons.get("roundEnd").x() + buttons.get("roundEnd").width() / 2,
				buttons.get("roundEnd").y() + buttons.get("roundEnd").height() / 2 + 10);
		graphics.setFont(getFont(16));
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
		this.mouseCoords = MouseInfo.getPointerInfo().getLocation();
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
		case PLAYING_BLIND -> {
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
				case 10:
					return "q";
				}
			}
		}
		case FINISHED_BLIND -> {
			if (state.getBlinds().get((state.getRound() - 1) % 3).score() < state.getCurrentBlindScore()) {
				if (isButtonClick(mouseClick, state.getHandSize()) == 4) {
					return "";
				}
			}
		}
		case SHOP -> {
			int input = isButtonClick(mouseClick, state.getHandSize());
			if (input != -1 && input < 3) {
				return String.valueOf(input);
			}
			if (input == 3) {
				return "e";
			}
			if( input == 10) {
				return "q";
			}
		}
		case GAME_OVER, VICTORY -> {
			if (buttons.get("playAgain").isClicked(mouseClick)) {
				return "r";
			}
			if(buttons.get("quit").isClicked(mouseClick)) {
				return "q";
			}
		}
		default -> {
			return null;
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
		if (buttons.get("planet0").isClicked(mouseClick)) {
			return 0;
		}
		if (buttons.get("planet1").isClicked(mouseClick)) {
			return 1;
		}
		if (buttons.get("planet2").isClicked(mouseClick)) {
			return 2;
		}
		if (buttons.get("exitShop").isClicked(mouseClick)) {
			return 3;
		}
		if(buttons.get("quit").isClicked(mouseClick)) {
			return 10;
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

	/**
	 * Dessine un conteneur (fond arrondi sombre + liseré) pour un élément de la
	 * barre d'info latérale.
	 */
	private void drawInfoContainer(Graphics2D graphics, int x, int y, int width, int height) {
		graphics.setColor(INFO_PANEL);
		graphics.fillRoundRect(x, y, width, height, 18, 18);
		graphics.setColor(INFO_PANEL_BORDER);
		graphics.setStroke(new BasicStroke(2));
		graphics.drawRoundRect(x, y, width, height, 18, 18);
	}

	// Facteur d'échelle appliqué à tout le texte (léger scale down global)
	private static final float FONT_SCALE = 0.82f;

	private Font getFont(float size) {
		float scaled = size * FONT_SCALE;
		if (this.font == null) {
			return new Font("Arial", Font.BOLD, (int) scaled);
		}
		return this.font.deriveFont(scaled);
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
