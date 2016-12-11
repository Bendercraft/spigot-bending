package net.bendercraft.spigot.bending.db;

import java.util.List;
import java.util.Map;

import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPerk;

public class SkillTree {
	private Map<BendingElement, Integer> points;
	private List<BendingPerk> skills;
	public Map<BendingElement, Integer> getPoints() {
		return points;
	}
	public void setPoints(Map<BendingElement, Integer> points) {
		this.points = points;
	}
	public List<BendingPerk> getSkills() {
		return skills;
	}
	public void setSkills(List<BendingPerk> skills) {
		this.skills = skills;
	}
}
