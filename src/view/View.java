package view;

import java.util.List;
import controller.PlayResult;
import model.GameState;

public sealed interface View permits ConsoleView, ZenView {

	void render(GameState state);

	void renderHelp();

	void renderInvalidInput(GameState state, String message);

	void renderPlayResult(GameState state, PlayResult result);
	
	List<String> getUserInput(GameState state);
}
