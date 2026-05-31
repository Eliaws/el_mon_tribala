package view;

import com.github.forax.zen.PointerEvent.Location;

public record Rect(int x, int y, int width, int height) {

	public Rect {
		if (x < 0 || y < 0 || width < 0 || height < 0) {
			throw new IllegalArgumentException();
		}
	}
	
	boolean isClicked(Location click) {
		return click.x() >= x
                && click.x() <= x + width
                && click.y() >= y
                && click.y() <= y + height;
    }
}
