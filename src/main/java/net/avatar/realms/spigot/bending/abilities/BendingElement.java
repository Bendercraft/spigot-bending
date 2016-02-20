package net.avatar.realms.spigot.bending.abilities;

public enum BendingElement {

	None, Air, Water, Earth, Fire, Master, Energy;

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
			case Air:
				return BendingPath.Spiritual;
			case Earth:
				return BendingPath.Patient;
			case Water:
				return BendingPath.Balanced;
			case Fire:
				return BendingPath.Control;
			case Master:
				return BendingPath.Equality;
			default:
				return BendingPath.None;
				
		}
	}
}
