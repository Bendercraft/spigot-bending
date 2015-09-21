package net.avatar.realms.spigot.bending.abilities;

public enum BendingPath {
	None(BendingElement.None),

	Spiritual(BendingElement.Air), // Default
	Mobile(BendingElement.Air), Renegade(BendingElement.Air),

	Balanced(BendingElement.Water), // Default
	Marksman(BendingElement.Water), Flowless(BendingElement.Water),

	Control(BendingElement.Fire), // Default
	Nurture(BendingElement.Fire), Lifeless(BendingElement.Fire),

	Patient(BendingElement.Earth), // Default
	Tough(BendingElement.Earth), Reckless(BendingElement.Earth),

	Equality(BendingElement.ChiBlocker), // Default
	Seeker(BendingElement.ChiBlocker), Restless(BendingElement.ChiBlocker);

	private BendingElement element;

	BendingPath(BendingElement element) {
		this.element = element;
	}

	public static BendingPath getType(String string) {
		for (BendingPath type : BendingPath.values()) {
			if (type.toString().equalsIgnoreCase(string))
				return type;
		}
		return null;
	}

	public BendingElement getElement() {
		return element;
	}

}
