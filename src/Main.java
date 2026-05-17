import controller.GameController;
import view.ConsoleView;

public class Main {

	public static void main(String[] args) {
		new ConsoleView(new GameController()).run();
	}
}
