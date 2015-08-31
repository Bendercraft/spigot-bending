package net.avatar.realms.spigot.bending.abilities;

public enum BendingPathType {
	None 		(BendingType.None),
	
	Spiritual	(BendingType.Air),
	Mobile		(BendingType.Air),
	Renegade	(BendingType.Air),
	
	Balanced	(BendingType.Water),
	Marksman	(BendingType.Water),
	Flowless	(BendingType.Water),
	
	Control		(BendingType.Fire),
	Nurture		(BendingType.Fire),
	Lifeless	(BendingType.Fire),
	
	Patient		(BendingType.Earth),
	Tough		(BendingType.Earth),
	Reckless	(BendingType.Earth),
	
	Equality	(BendingType.ChiBlocker),
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
