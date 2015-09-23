package net.avatar.realms.spigot.bending.abilities;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;

public class BendingPlayerData {
	private UUID player;
	private List<BendingElement> bendings;
	private List<BendingAffinity> affinities;
	private List<BendingPath> paths;
	private boolean bendToItem;
	private Map<String, Map<Integer, BendingAbilities>> decks;
	private Map<Material, BendingAbilities> itemAbilities;
	private long lastTime;

	public UUID getPlayer() {
		return this.player;
	}

	public void setPlayer(UUID player) {
		this.player = player;
	}

	public List<BendingElement> getBendings() {
		return this.bendings;
	}

	public void setBendings(List<BendingElement> bending) {
		this.bendings = bending;
	}

	public boolean isBendToItem() {
		return this.bendToItem;
	}

	public void setBendToItem(boolean bendToItem) {
		this.bendToItem = bendToItem;
	}

	public Map<String, Map<Integer, BendingAbilities>> getDecks() {
		return this.decks;
	}

	public void setDecks(Map<String, Map<Integer, BendingAbilities>> slotAbilities) {
		this.decks = slotAbilities;
	}

	public Map<Material, BendingAbilities> getItemAbilities() {
		return this.itemAbilities;
	}

	public void setItemAbilities(Map<Material, BendingAbilities> itemAbilities) {
		this.itemAbilities = itemAbilities;
	}

	public long getLastTime() {
		return this.lastTime;
	}

	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}

	public List<BendingAffinity> getAffinities() {
		return this.affinities;
	}

	public void setAffinities(List<BendingAffinity> affinities) {
		this.affinities = affinities;
	}

	public List<BendingPath> getPaths() {
		return this.paths;
	}

	public void setPaths(List<BendingPath> paths) {
		this.paths = paths;
	}
}
