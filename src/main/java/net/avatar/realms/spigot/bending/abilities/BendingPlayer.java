package net.avatar.realms.spigot.bending.abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.event.AbilityCooldownEvent;

public class BendingPlayer {

	private UUID player;

	private String currentDeck = "default";
	private Map<String, Map<Integer, String>> decks = new HashMap<String, Map<Integer, String>>();

	private List<BendingElement> bendings = new LinkedList<BendingElement>();
	private List<BendingAffinity> affinities = new LinkedList<BendingAffinity>();
	private List<BendingPath> paths = new LinkedList<BendingPath>();

	private Map<String, Long> cooldowns = new HashMap<String, Long>();

	private long paralyzeTime = 0;
	private long slowTime = 0;

	private long lastTime = 0;

	public BendingPlayer(UUID id) {
		this.player = id;
		this.lastTime = System.currentTimeMillis();
		this.decks.put(this.currentDeck, new TreeMap<Integer, String>());
	}

	public String getCurrentDeck() {
		return this.currentDeck;
	}

	public Set<String> getDecksNames() {
		return this.decks.keySet();
	}

	public Map<String, Map<Integer, String>> getDecks() {
		return this.decks;
	}

	public void setCurrentDeck(String current) {
		this.currentDeck = current;
	}

	public BendingPlayer(BendingPlayerData data) {
		this.player = data.getPlayer();

		this.bendings = data.getBendings() != null ? data.getBendings() : this.bendings;
		this.affinities = data.getAffinities() != null ? data.getAffinities() : this.affinities;
		this.paths = data.getPaths() != null ? data.getPaths() : this.paths;

		this.decks = data.getDecks() != null ? data.getDecks() : this.decks;
		this.currentDeck = data.getCurrentDeck();

		this.lastTime = data.getLastTime();
	}

	public static BendingPlayer getBendingPlayer(Player player) {
		return getBendingPlayer(player.getUniqueId());
	}
	
	public static BendingPlayer getBendingPlayer(UUID player) {
		return Bending.getInstance().getBendingDatabase().get(player);
	}

	public boolean isOnGlobalCooldown() {
		return System.currentTimeMillis() <= (this.lastTime + Settings.GLOBAL_COOLDOWN);
	}

	public boolean isOnCooldown(String ability) {

		if (isOnGlobalCooldown()) {
			return true;
		}

		if (this.cooldowns.containsKey(ability)) {
			long time = System.currentTimeMillis();
			return time <= this.cooldowns.get(ability);

		} else {
			return false;
		}
	}

	public void cooldown() {
		this.lastTime = System.currentTimeMillis();
	}

	public void cooldown(BendingAbility ability, long cooldownTime) {
		cooldown(ability.getName(), cooldownTime);
	}

	public void cooldown(String ability, long cooldownTime) {
		long time = System.currentTimeMillis();
		if (ability != null) {
			this.cooldowns.put(ability, time + cooldownTime);
		}
		this.lastTime = time;
		if (ability != null) {
			Bending.callEvent(new AbilityCooldownEvent(this, ability));
		}
	}

	public Map<String, Long> getCooldowns() {
		Map<String, Long> result = new HashMap<String, Long>();
		long now = System.currentTimeMillis();
		List<String> toRemove = new LinkedList<String>();
		for (String ab : cooldowns.keySet()) {
			long remain = cooldowns.get(ab) - now;
			if (remain <= 0) {
				toRemove.add(ab);
			} else {
				result.put(ab, remain);
			}
		}

		for (String ab : toRemove) {
			result.remove(ab);
		}

		return result;
	}

	public UUID getPlayerID() {
		return this.player;
	}

	public boolean isBender() {
		return !this.bendings.isEmpty();
	}

	public boolean isBender(BendingElement type) {
		if (type == BendingElement.Energy) {
			return true;
		}
		return this.bendings.contains(type);
	}

	public boolean hasAffinity(BendingAffinity specialization) {
		return this.affinities.contains(specialization);
	}

	public boolean hasPath(BendingElement type) {
		if (this.paths == null) {
			return false;
		}
		for (BendingPath path : this.paths) {
			if (path.getElement() == type) {
				return true;
			}
		}
		return false;
	}

	public boolean hasPath(BendingPath path) {
		if (this.paths == null) {
			return false;
		}
		return this.paths.contains(path);
	}

	public void setBender(BendingElement type) {
		removeBender();
		this.bendings.add(type);
		setPath(type.getDefaultPath());
		Bending.getInstance().getBendingDatabase().save(this.player);
	}

	public void addBender(BendingElement type) {
		if (!this.bendings.contains(type)) {
			this.bendings.add(type);
			Bending.getInstance().getBendingDatabase().save(this.player);
		}
	}

	public void setAffinity(BendingAffinity affinity) {
		this.clearAffinity(affinity.getElement());
		this.affinities.add(affinity);
		Bending.getInstance().getBendingDatabase().save(this.player);
	}

	public void setPath(BendingPath path) {
		this.clearPath(path.getElement());
		this.paths.add(path);
		Bending.getInstance().getBendingDatabase().save(this.player);
	}

	public void addAffinity(BendingAffinity affinity) {
		if (!this.affinities.contains(affinity)) {
			this.affinities.add(affinity);
			Bending.getInstance().getBendingDatabase().save(this.player);
		}
	}

	public void removeAffinity(BendingAffinity affinity) {
		this.affinities.remove(affinity);
		clearAbilities();
		// clear abilities will save for us
	}

	public void clearAffinity(BendingElement element) {
		List<BendingAffinity> toRemove = new LinkedList<BendingAffinity>();
		for (BendingAffinity spe : this.affinities) {
			if (spe.getElement().equals(element)) {
				toRemove.add(spe);
			}
		}
		for (BendingAffinity spe : toRemove) {
			removeAffinity(spe);
		}
		// clear abilities will save for us
		clearAbilities();
	}

	public void clearPath(BendingElement element) {
		List<BendingPath> toRemove = new LinkedList<BendingPath>();
		for (BendingPath path : this.paths) {
			if (path.getElement().equals(element)) {
				toRemove.add(path);
			}
		}
		for (BendingPath path : toRemove) {
			this.paths.remove(path);
		}
	}

	public void clearAffinities() {
		this.affinities.clear();
		Bending.getInstance().getBendingDatabase().save(this.player);
	}

	public void clearAbilities() {
		this.decks.put(this.currentDeck, new HashMap<Integer, String>());
		Bending.getInstance().getBendingDatabase().save(this.player);
	}

	public void removeBender() {
		clearAbilities();
		this.affinities.clear();
		this.bendings.clear();
		this.paths.clear();
		Bending.getInstance().getBendingDatabase().save(this.player);
	}

	public String getAbility() {
		Player playerEntity = getPlayer();
		if (playerEntity == null) {
			return null;
		}
		if (!playerEntity.isOnline() || playerEntity.isDead()) {
			return null;
		}

		int slot = playerEntity.getInventory().getHeldItemSlot();
		return getAbility(slot);
	}

	public Map<Integer, String> getAbilities() {
		return this.decks.get(this.currentDeck);
	}

	public String getAbility(int slot) {
		if(!this.decks.containsKey(this.currentDeck)) {
			return null;
		}
		return this.decks.get(this.currentDeck).get(slot);
	}

	public void setAbility(int slot, String ability) {
		if(!this.decks.containsKey(this.currentDeck)) {
			Bending.getInstance().getLogger().warning("Player "+this.player+" tried to bind an ability on unknown deck "+this.currentDeck);
			return;
		}
		this.decks.get(this.currentDeck).put(slot, ability);
		Bending.getInstance().getBendingDatabase().save(this.player);
	}

	public void removeSelectedAbility() {
		Player p = getPlayer();
		if (p == null) {
			return;
		}
		if (!p.isOnline() || p.isDead()) {
			return;
		}

		int slot = p.getInventory().getHeldItemSlot();
		removeAbility(slot);

		Bending.getInstance().getBendingDatabase().save(this.player);
	}

	public void removeAbility(int slot) {
		setAbility(slot, null);
		Bending.getInstance().getBendingDatabase().save(this.player);
	}

	public Player getPlayer() {
		return Bukkit.getServer().getPlayer(this.player);
	}

	public List<BendingElement> getBendingTypes() {
		List<BendingElement> list = new ArrayList<BendingElement>();
		for (BendingElement index : this.bendings) {
			list.add(index);
		}
		return list;
	}

	public String bendingsToString() {

		Player pl = getPlayer();
		if (pl != null) {
			String str = pl.getName() + " : \n";
			for (BendingElement type : this.bendings) {
				str += type.toString() + "\n";
			}
			return str;
		}
		return "This player seems not to exist.";

	}

	public boolean canBeParalyzed() {
		return System.currentTimeMillis() > this.paralyzeTime;
	}

	public boolean canBeSlowed() {
		return System.currentTimeMillis() > this.slowTime;
	}

	public void paralyze(long cooldown) {
		this.paralyzeTime = System.currentTimeMillis() + cooldown;
	}

	public void slow(long cooldown) {
		this.slowTime = System.currentTimeMillis() + cooldown;
	}

	public long getLastTime() {
		return this.lastTime;
	}

	public void delete() {
		Bending.getInstance().getBendingDatabase().remove(this.player);
	}

	@Override
	public String toString() {
		String string = "BendingPlayer{";
		string += "Player=" + this.player.toString();
		string += ", ";
		string += "Bendings=" + this.bendings;
		string += ", ";
		string += "Decks=" + this.decks;
		string += "}";
		return string;
	}

	public List<BendingAffinity> getAffinities() {
		return this.affinities;
	}

	public List<BendingPath> getPath() {
		return this.paths;
	}

	public BendingPlayerData serialize() {
		BendingPlayerData result = new BendingPlayerData();
		result.setBendings(this.bendings);
		result.setLastTime(this.lastTime);
		result.setAffinities(this.affinities);
		result.setPlayer(this.player);
		result.setDecks(this.decks);
		result.setCurrentDeck(this.currentDeck);
		result.setPaths(this.paths);
		return result;
	}

}
