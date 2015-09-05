package net.avatar.realms.spigot.bending.abilities;

public enum BendingPathType {
	None 		(BendingType.None),
	
	Spiritual	(BendingType.Air), // Default
	Mobile		(BendingType.Air),
	Renegade	(BendingType.Air),
	
	Balanced	(BendingType.Water), // Default
	Marksman	(BendingType.Water),
	Flowless	(BendingType.Water),
	
	Control		(BendingType.Fire), // Default
	Nurture		(BendingType.Fire),
	Lifeless	(BendingType.Fire),
	
	Patient		(BendingType.Earth), // Default
	Tough		(BendingType.Earth),
	Reckless	(BendingType.Earth),
	
	Equality	(BendingType.ChiBlocker), // Default
	Seeker		(BendingType.ChiBlocker),
	Restless	(BendingType.ChiBlocker);

	private BendingType element;
	
	BendingPathType(BendingType element) {
		this.element = element;
	}
	
	public static BendingPathType getType(String string) {
		for (BendingPathType type : BendingPathType.values()) {
			if (type.toString().equalsIgnoreCase(string))
				return type;
		}
		return null;
	}
	
	public BendingType getElement() {
		return element;
	}

}
