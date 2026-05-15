package domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import domain.consummables.Planet;

public class Shop {
	public static final int SLOT_COUNT = 2;
	public static final int PLANET_PRICE = 3;

	private final List<Planet> offers;
	private final Random random;

	public Shop() {
		this.offers = new ArrayList<>();
		this.random = new Random();
	}

	public void regenerate() {
		offers.clear();
		Planet[] all = Planet.values();
		for (int i = 0; i < SLOT_COUNT; i++) {
			offers.add(all[random.nextInt(all.length)]);
		}
	}

	public List<Planet> getOffers() {
		return offers;
	}

	public void remove(int index) {
		offers.remove(index);
	}
}
