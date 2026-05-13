package domain.consummables;

public enum Planet {
    Pluto(1,10),
    Mercury(1,15),
    Uranus(1,20),
    Venus(2,20),
    Saturn(3,30),
    Jupiter(2, 15),
    Earth(2, 25),
    Mars(3, 30),
    Neptune(4, 40),
    PlanetX(3, 35),
    Ceres(4, 40),
    Eris(3, 50);

    private int mult;
    private int chips;

	Planet(int mult, int chips) {
		this.chips = chips;
		this.mult = mult;
	}

	public int getChips() {
		return chips;
	}

	public int getMult() {
		return mult;
	}
}    
