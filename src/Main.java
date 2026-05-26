import controller.GameController;
import view.ConsoleView;
import view.View;

public class Main {

	public static void main(String[] args) {
		GameController controller = new GameController();
		View view = new ConsoleView(controller);
		view.run();
	}
}
