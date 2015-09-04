package net.avatar.realms.spigot.bending.abilities;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;

public class BendingPlayerData {
	private UUID player;
	private List<BendingType> bendings;
	private List<BendingSpecializationType> specialization;
	private List<BendingPathType> paths;
	private boolean bendToItem;
	private Map<Integer, Abilities> slotAbilities;
	private Map<Material, Abilities> itemAbilities;
	private long lastTime;
	
	
	public UUID getPlayer() {
		return player;
	}
	public void setPlayer(UUID player) {
		this.player = player;
	}
	public List<BendingType> getBendings() {
		return bendings;
	}
	public void setBendings(List<BendingType> bending) {
		this.bendings = bending;
	}
	public boolean isBendToItem() {
		return bendToItem;
	}
	public void setBendToItem(boolean bendToItem) {
		this.bendToItem = bendToItem;
	}
	public Map<Integer, Abilities> getSlotAbilities() {
		return slotAbilities;
	}
	public void setSlotAbilities(Map<Integer, Abilities> slotAbilities) {
		this.slotAbilities = slotAbilities;
	}
	public Map<Material, Abilities> getItemAbilities() {
		return itemAbilities;
	}
	public void setItemAbilities(Map<Material, Abilities> itemAbilities) {
		this.itemAbilities = itemAbilities;
	}
	public long getLastTime() {
		return lastTime;
	}
	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}
	public List<BendingSpecializationType> getSpecialization() {
		return specialization;
	}
	public void setSpecialization(List<BendingSpecializationType> specialization) {
		this.specialization = specialization;
	}
	public List<BendingPathType> getPaths() {
		return paths;
	}
	public void setPaths(List<BendingPathType> paths) {
		this.paths = paths;
	}
}
