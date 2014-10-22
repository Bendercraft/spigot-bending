package net.avatarrealms.minecraft.bending.abilities;

public enum BendingSpecializationType {

	Tornado(BendingType.Air), 
	Suffocate(BendingType.Air), 
	
	Bloodbend(BendingType.Water), 
	DrainBend(BendingType.Water), 
	
	Lightning(BendingType.Fire), 
	Combustion(BendingType.Fire), 
	
	Metalbend(BendingType.Earth), 
	Lavabend(BendingType.Earth),
	
	Inventor(BendingType.ChiBlocker);
	
	

	private BendingType element;
	
	BendingSpecializationType(BendingType element) {
		this.element = element;
	}
	
	public static BendingSpecializationType getType(String string) {
		for (BendingSpecializationType type : BendingSpecializationType.values()) {
			if (type.toString().equalsIgnoreCase(string))
				return type;
		}
		return null;
	}
	
	public BendingType getElement() {
		return element;
	}

}
