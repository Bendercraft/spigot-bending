package net.bendercraft.spigot.bending.abilities;

public enum BendingAffinity {
	NONE(BendingElement.NONE),

	TORNADO(BendingElement.AIR), SUFFOCATE(BendingElement.AIR),

	BLOOD(BendingElement.WATER), DRAIN(BendingElement.WATER),

	LIGHTNING(BendingElement.FIRE), COMBUSTION(BendingElement.FIRE),

	METAL(BendingElement.EARTH), LAVA(BendingElement.EARTH),

	CHI(BendingElement.MASTER), BOW(BendingElement.MASTER), SWORD(BendingElement.MASTER);

	private BendingElement element;

	BendingAffinity(BendingElement element) {
		this.element = element;
	}

	public static BendingAffinity getType(String string) {
		for (BendingAffinity type : BendingAffinity.values()) {
			if (type.toString().equalsIgnoreCase(string)) {
				return type;
			}
		}
		return null;
	}

	public BendingElement getElement() {
		return this.element;
	}

}
