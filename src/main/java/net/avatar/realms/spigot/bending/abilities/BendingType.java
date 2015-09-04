package net.avatar.realms.spigot.bending.abilities;

public enum BendingType {

	None, Air, Water, Earth, Fire, ChiBlocker, Energy;

	public static BendingType getType (String string) {
		for (BendingType type : BendingType.values()) {
			if (type.toString().equalsIgnoreCase(string)) {
				return type;
			}
		}
		return null;
	}
}
