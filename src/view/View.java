package view;

import controller.PlayResult;
import model.GameState;

public sealed interface View permits ConsoleView {

	public void render(GameState gameState);

	public String getUserInput();

	public void renderInvalidInput(GameState gameState, String input);

	public void renderHelp();

	public void renderPlayResult(GameState gameState, PlayResult result);

}
