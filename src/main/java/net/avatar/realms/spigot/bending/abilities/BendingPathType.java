package net.avatar.realms.spigot.bending.abilities;

public enum BendingPathType {
	None 		(BendingType.None),
	
	Spiritual	(BendingType.Air), // Default
	Mobile		(BendingType.Air), // Add mobility - Reduce speed
	Renegade	(BendingType.Air), // Add damage - Reduce range & Augmented cooldown
	
	Balanced	(BendingType.Water), // Default
	Marksman	(BendingType.Water), // Add range - Reduce damage
	Flowless	(BendingType.Water), // Add damage on ice - Remove redirection & control in-flight
	
	Control		(BendingType.Fire), // Default
	Nurture		(BendingType.Fire), // Add new DoT system - Reduce direct damage
	Lifeless	(BendingType.Fire), // Add direct damage - Remove entirely DoT
	
	Patient		(BendingType.Earth), // Default
	Tough		(BendingType.Earth), // Add resistance - Reduce damage
	Reckless	(BendingType.Earth), // Add damage - Reduce speed
	
	Equality	(BendingType.ChiBlocker), // Default
	Seeker		(BendingType.ChiBlocker), // Reduce cooldown - Reduce damage
	Restless	(BendingType.ChiBlocker); // Add resistance - Augment cooldown

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
