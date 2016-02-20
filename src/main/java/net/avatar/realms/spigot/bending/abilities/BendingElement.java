package net.avatar.realms.spigot.bending.abilities;

public enum BendingElement {

	NONE, MASTER, AIR, WATER, EARTH, FIRE, ENERGY;

	public static BendingElement getType(String string) {
		for (BendingElement type : BendingElement.values()) {
			if (type.toString().equalsIgnoreCase(string)) {
				return type;
			}
		}
		return null;
	}
	
	public BendingPath getDefaultPath () {
		switch (this) {
			case AIR:
				return BendingPath.SPIRITUAL;
			case EARTH:
				return BendingPath.PATIENT;
			case WATER:
				return BendingPath.BALANCED;
			case FIRE:
				return BendingPath.CONTROL;
			case MASTER:
				return BendingPath.FREE;
			default:
				return BendingPath.NONE;
				
		}
	}
}
