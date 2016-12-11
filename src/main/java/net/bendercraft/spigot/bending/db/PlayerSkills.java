package net.bendercraft.spigot.bending.db;

import java.util.List;
import java.util.UUID;

public class PlayerSkills {
	private UUID player;
	private List<String> skills;
	
	public UUID getPlayer() {
		return player;
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
