package net.avatarrealms.minecraft.bending.model.data;

import net.avatarrealms.minecraft.bending.model.BendingType;


public class BendingLevelData {
	private BendingType bendingType;
	private int level;
	private int experience;
	public BendingType getBendingType() {
		return bendingType;
	}
	public void setBendingType(BendingType bendingType) {
		this.bendingType = bendingType;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getExperience() {
		return experience;
	}
	public void setExperience(int experience) {
		this.experience = experience;
	}
}
