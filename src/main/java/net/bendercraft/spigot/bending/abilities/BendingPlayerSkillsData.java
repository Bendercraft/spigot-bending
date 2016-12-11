package net.bendercraft.spigot.bending.abilities;

import java.util.List;
import java.util.UUID;

public class BendingPlayerSkillsData {
	private UUID player;
	private List<String> skills;

	public UUID getPlayer() {
		return this.player;
	}

	public void setPlayer(UUID player) {
		this.player = player;
	}

	public List<String> getSkills() {
		return skills;
	}

	public void setSkills(List<String> skills) {
		this.skills = skills;
	}
}
