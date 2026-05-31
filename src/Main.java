import controller.GameController;
import view.ConsoleView;
import view.View;
import view.ZenView;

public class Main {

	public static void main(String[] args) {
		View view = new ConsoleView();
		GameController controller = new GameController(view);
		controller.run();
	}
}
