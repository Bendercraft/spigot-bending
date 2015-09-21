package net.avatar.realms.spigot.bending.abilities;

public enum BendingElement {

	None, Air, Water, Earth, Fire, ChiBlocker, Energy;

	public static BendingElement getType (String string) {
		for (BendingElement type : BendingElement.values()) {
			if (type.toString().equalsIgnoreCase(string)) {
				return type;
			}
		}
		return null;
	}
}
