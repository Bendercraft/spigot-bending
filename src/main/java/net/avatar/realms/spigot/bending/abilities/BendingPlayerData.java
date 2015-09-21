package net.avatar.realms.spigot.bending.abilities;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;

public class BendingPlayerData {
	private UUID player;
	private List<BendingElement> bendings;
	private List<BendingAffinity> specialization;
	private List<BendingPath> paths;
	private boolean bendToItem;
	private Map<Integer, BendingAbilities> slotAbilities;
	private Map<Material, BendingAbilities> itemAbilities;
	private long lastTime;

	public UUID getPlayer() {
		return player;
	}

	public void setPlayer(UUID player) {
		this.player = player;
	}

	public List<BendingElement> getBendings() {
		return bendings;
	}

	public void setBendings(List<BendingElement> bending) {
		this.bendings = bending;
	}

	public boolean isBendToItem() {
		return bendToItem;
	}

	public void setBendToItem(boolean bendToItem) {
		this.bendToItem = bendToItem;
	}

	public Map<Integer, BendingAbilities> getSlotAbilities() {
		return slotAbilities;
	}

	public void setSlotAbilities(Map<Integer, BendingAbilities> slotAbilities) {
		this.slotAbilities = slotAbilities;
	}

	public Map<Material, BendingAbilities> getItemAbilities() {
		return itemAbilities;
	}

	public void setItemAbilities(Map<Material, BendingAbilities> itemAbilities) {
		this.itemAbilities = itemAbilities;
	}

	public long getLastTime() {
		return lastTime;
	}

	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}

	public List<BendingAffinity> getSpecialization() {
		return specialization;
	}

	public void setSpecialization(List<BendingAffinity> specialization) {
		this.specialization = specialization;
	}

	public List<BendingPath> getPaths() {
		return paths;
	}

	public void setPaths(List<BendingPath> paths) {
		this.paths = paths;
	}
}
