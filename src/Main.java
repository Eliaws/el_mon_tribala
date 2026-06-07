import controller.GameController;
import view.ConsoleView;
import view.View;
import view.ZenView;

public class Main {

	public static void main(String[] args) {
		if (args.length != 1) {
			IO.println("Usage: java Main [console|zen]");
			return;
		}
		View view;
		if (args.length == 1) {
			if (!args[0].equals("zen") && !args[0].equals("console")) {
				IO.println("Usage: java Main [console|zen]");
				return;
			}
			if (args[0].equals("console")) {
				view = new ConsoleView();
				GameController controller = new GameController(view);
				controller.run();
				return;
			} 
			else if(args[0].equals("zen")) {
				view = new ZenView();
				GameController controller = new GameController(view);
				controller.run();
				return;
			}
		}

	}
}
