package tools;

import java.util.Arrays;

public enum BendingType {

	Air, Water, Earth, Fire, ChiBlocker;

	public static BendingType getType(String string) {
		for (BendingType type : BendingType.values()) {
			if (type.toString().equalsIgnoreCase(string))
				return type;
		}
		return null;
	}

	public static int getIndex(BendingType type) {
		if (type == null)
			return -1;
		return Arrays.asList(BendingType.values()).indexOf(type);
	}

	public static BendingType getType(int index) {
		if (index == -1)
			return null;
		return Arrays.asList(BendingType.values()).get(index);
	}

}
