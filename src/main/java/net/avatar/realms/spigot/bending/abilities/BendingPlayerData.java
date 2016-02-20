package net.avatar.realms.spigot.bending.abilities;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BendingPlayerData {
	private UUID player;
	private List<BendingElement> bendings;
	private List<BendingAffinity> affinities;
	private List<BendingPath> paths;
	private Map<String, Map<Integer, String>> decks;
	private String currentDeck;
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

	public Map<String, Map<Integer, String>> getDecks() {
		return this.decks;
	}

	public void setDecks(Map<String, Map<Integer, String>> slotAbilities) {
		this.decks = slotAbilities;
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

	public String getCurrentDeck() {
		return this.currentDeck;
	}

	public void setCurrentDeck(String currentDeck) {
		this.currentDeck = currentDeck;
	}
}
