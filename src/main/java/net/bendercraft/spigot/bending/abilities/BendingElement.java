package net.bendercraft.spigot.bending.abilities;

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
}
