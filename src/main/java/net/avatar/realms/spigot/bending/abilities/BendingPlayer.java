package net.avatar.realms.spigot.bending.abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.event.AbilityCooldownEvent;


public class BendingPlayer {

	private UUID player;

	private Map<Integer, Abilities> slotAbilities = new HashMap<Integer, Abilities>();

	private List<BendingType> bendings = new LinkedList<BendingType>();
	private List<BendingSpecializationType> specializations = new LinkedList<BendingSpecializationType>();
	private List<BendingPathType> paths = new LinkedList<BendingPathType>();

	private Map<Abilities, Long> cooldowns = new HashMap<Abilities, Long>();

	private long paralyzeTime = 0;
	private long slowTime = 0;

	private long lastTime = 0;

	private boolean tremorsense = true;

	public BendingPlayer (UUID id) {
		this.player = id;
		this.lastTime = System.currentTimeMillis();
	}

	public BendingPlayer (BendingPlayerData data) {
		this.player = data.getPlayer();

		this.bendings = data.getBendings();
		this.slotAbilities = data.getSlotAbilities();

		this.specializations = data.getSpecialization();
		this.paths = data.getPaths();
		if(this.paths == null) {
			this.paths = new LinkedList<BendingPathType>();
		}

		this.lastTime = data.getLastTime();
	}

	public static BendingPlayer getBendingPlayer (Player player) {
		return Bending.database.get(player.getUniqueId());
	}

	public boolean isOnGlobalCooldown () {
		return (System.currentTimeMillis() <= (this.lastTime + Settings.GLOBAL_COOLDOWN));
	}

	public boolean isOnCooldown (Abilities ability) {

		if (isOnGlobalCooldown()) {
			return true;
		}

		if (this.cooldowns.containsKey(ability)) {
			long time = System.currentTimeMillis();
			return (time <= this.cooldowns.get(ability));

		}
		else {
			return false;
		}
	}

	public void toggleTremorsense () {
		this.tremorsense = !this.tremorsense;
	}

	public boolean isTremorsensing () {
		return this.tremorsense;
	}

	public void cooldown () {
		cooldown(null, 0);
	}

	public void cooldown (Abilities ability, long cooldownTime) {
		long time = System.currentTimeMillis();
		if (ability != null) {
			this.cooldowns.put(ability, time + cooldownTime);
		}
		this.lastTime = time;
		if (ability != null) {
			Bending.callEvent(new AbilityCooldownEvent(this, ability));
		}
	}

	public Map<Abilities, Long> getCooldowns() {
		Map<Abilities, Long> cooldowns = new HashMap<Abilities, Long>();
		long now = System.currentTimeMillis();
		List<Abilities> toRemove = new LinkedList<Abilities>();
		for (Abilities ab : this.cooldowns.keySet()) {
			long remain = this.cooldowns.get(ab) - now;
			if (remain <= 0) {
				toRemove.add(ab);
			}
			else {
				cooldowns.put(ab, remain);
			}
		}

		for (Abilities ab : toRemove) {
			cooldowns.remove(ab);
		}

		return cooldowns;
	}

	public UUID getPlayerID () {
		return this.player;
	}

	public boolean isBender () {
		return !this.bendings.isEmpty();
	}

	public boolean isBender (BendingType type) {
		return this.bendings.contains(type);
	}

	public boolean isSpecialized (BendingSpecializationType specialization) {
		return this.specializations.contains(specialization);
	}
	
	public boolean hasPath(BendingType type) {
		if(paths == null) {
			return false;
		}
		for(BendingPathType path : paths) {
			if(path.getElement() == type) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasPath(BendingPathType path) {
		if(paths == null) {
			return false;
		}
		return this.paths.contains(path);
	}

	public void setBender (BendingType type) {
		removeBender();
		this.bendings.add(type);
		Bending.database.save(this.player);
	}

	public void addBender (BendingType type) {
		if (!this.bendings.contains(type)) {
			this.bendings.add(type);
			Bending.database.save(this.player);
		}
	}

	public void setSpecialization (BendingSpecializationType specialization) {
		this.clearSpecialization(specialization.getElement());
		this.specializations.add(specialization);
		Bending.database.save(this.player);
	}
	
	public void setPath(BendingPathType path) {
		this.clearPath(path.getElement());
		this.paths.add(path);
		Bending.database.save(this.player);
	}

	public void addSpecialization (BendingSpecializationType specialization) {
		if (!this.specializations.contains(specialization)) {
			this.specializations.add(specialization);
			Bending.database.save(this.player);
		}
	}

	public void removeSpecialization (BendingSpecializationType specialization) {
		this.specializations.remove(specialization);
		clearAbilities();
		// clear abilities will save for us
	}

	public void clearSpecialization (BendingType element) {
		List<BendingSpecializationType> toRemove = new LinkedList<BendingSpecializationType>();
		for (BendingSpecializationType spe : this.specializations) {
			if (spe.getElement().equals(element)) {
				toRemove.add(spe);
			}
		}
		for (BendingSpecializationType spe : toRemove) {
			removeSpecialization(spe);
		}
		// clear abilities will save for us
		clearAbilities();
	}
	
	public void clearPath(BendingType element) {
		List<BendingPathType> toRemove = new LinkedList<BendingPathType>();
		for (BendingPathType path : this.paths) {
			if (path.getElement().equals(element)) {
				toRemove.add(path);
			}
		}
		for (BendingPathType path : toRemove) {
			this.paths.remove(path);
		}
	}

	public void clearSpecialization () {
		this.specializations.clear();
		Bending.database.save(this.player);
	}

	public void clearAbilities () {
		this.slotAbilities = new HashMap<Integer, Abilities>();
		Bending.database.save(this.player);
	}

	public void removeBender () {
		clearAbilities();
		this.specializations.clear();
		this.bendings.clear();
		this.paths.clear();
		Bending.database.save(this.player);
	}

	public Abilities getAbility () {
		Player player = getPlayer();
		if (player == null) {
			return null;
		}
		if (!player.isOnline() || player.isDead()) {
			return null;
		}

		int slot = player.getInventory().getHeldItemSlot();
		return getAbility(slot);
	}

	public Abilities getAbility (int slot) {
		return this.slotAbilities.get(slot);
	}

	public void setAbility (int slot, Abilities ability) {
		this.slotAbilities.put(slot, ability);
		Bending.database.save(this.player);
	}

	public void removeSelectedAbility () {
		Player p = getPlayer();
		if (p == null) {
			return;
		}
		if (!p.isOnline() || p.isDead()) {
			return;
		}

		int slot = p.getInventory().getHeldItemSlot();
		removeAbility(slot);

		Bending.database.save(this.player);
	}

	public void removeAbility (int slot) {
		setAbility(slot, null);
		Bending.database.save(this.player);
	}

	public Player getPlayer () {
		return Bukkit.getServer().getPlayer(this.player);
	}

	public List<BendingType> getBendingTypes () {
		List<BendingType> list = new ArrayList<BendingType>();
		for (BendingType index : this.bendings) {
			list.add(index);
		}
		return list;
	}

	public String bendingsToString () {

		Player pl = getPlayer();
		if (pl != null) {
			String str = pl.getName() + " : \n";
			for (BendingType type : this.bendings) {
				str += type.toString() + "\n";
			}
			return str;
		}
		return "This player seems not to exist.";

	}

	public boolean canBeParalyzed () {
		return (System.currentTimeMillis() > this.paralyzeTime);
	}

	public boolean canBeSlowed () {
		return (System.currentTimeMillis() > this.slowTime);
	}

	public void paralyze (long cooldown) {
		this.paralyzeTime = System.currentTimeMillis() + cooldown;
	}

	public void slow (long cooldown) {
		this.slowTime = System.currentTimeMillis() + cooldown;
	}

	public long getLastTime () {
		return this.lastTime;
	}

	public void delete () {
		Bending.database.remove(this.player);
	}

	@Override
	public String toString () {
		String string = "BendingPlayer{";
		string += "Player=" + this.player.toString();
		string += ", ";
		string += "Bendings=" + this.bendings;
		string += ", ";
		string += "Binds=" + this.slotAbilities;
		string += "}";
		return string;
	}

	public List<BendingSpecializationType> getSpecializations() {
		return this.specializations;
	}
	
	public List<BendingPathType> getPath() {
		return this.paths;
	}

	public BendingPlayerData serialize () {
		BendingPlayerData result = new BendingPlayerData();
		result.setBendings(this.bendings);
		result.setLastTime(this.lastTime);
		result.setSpecialization(this.specializations);
		result.setPlayer(this.player);
		result.setSlotAbilities(this.slotAbilities);
		result.setPaths(this.paths);
		return result;
	}

}
