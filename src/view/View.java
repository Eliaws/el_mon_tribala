package view;

import controller.PlayResult;
import model.GameState;

public sealed interface View permits ConsoleView {

	void run();

	void render(GameState state);

	void renderHelp();

	void renderInvalidInput(GameState state, String message);

	void renderPlayResult(GameState state, PlayResult result);
}
