package view;

import java.util.List;
import model.GameState;

public sealed interface View permits ConsoleView, ZenView {

	void render(GameState state);

	void renderHelp();

	void renderInvalidInput(GameState state, String message);
	
	List<String> getUserInput(GameState state);
}
