package net.avatar.realms.spigot.bending.abilities;

public enum BendingPath {
	NONE(BendingElement.NONE),

	SPIRITUAL(BendingElement.AIR), // Default
	MOBILE(BendingElement.AIR), RENEGADE(BendingElement.AIR),

	BALANCED(BendingElement.WATER), // Default
	MARKSMAN(BendingElement.WATER), FLOWLESS(BendingElement.WATER),

	CONTROL(BendingElement.FIRE), // Default
	NURTURE(BendingElement.FIRE), LIFELESS(BendingElement.FIRE),

	PATIENT(BendingElement.EARTH), // Default
	TOUGH(BendingElement.EARTH), RECKLESS(BendingElement.EARTH),

	EQUALITY(BendingElement.MASTER), // Default
	SEEKER(BendingElement.MASTER), RESTLESS(BendingElement.MASTER);

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
