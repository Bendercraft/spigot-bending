package net.avatar.realms.spigot.bending.abilities;

public enum BendingAffinity {
	None(BendingElement.None),

	Tornado(BendingElement.Air),
	Suffocate(BendingElement.Air),

	Bloodbend(BendingElement.Water),
	DrainBend(BendingElement.Water),

	Lightning(BendingElement.Fire),
	Combustion(BendingElement.Fire),

	Metalbend(BendingElement.Earth),
	Lavabend(BendingElement.Earth),

	Inventor(BendingElement.ChiBlocker),
	Air(BendingElement.ChiBlocker),
	Earth(BendingElement.ChiBlocker),
	Fire(BendingElement.ChiBlocker),
	Water(BendingElement.ChiBlocker);

	private BendingElement element;

	BendingAffinity(BendingElement element) {
		this.element = element;
	}

	public static BendingAffinity getType(String string) {
		for (BendingAffinity type : BendingAffinity.values()) {
			if (type.toString().equalsIgnoreCase(string))
				return type;
		}
		return null;
	}

	public BendingElement getElement() {
		return element;
	}

}
