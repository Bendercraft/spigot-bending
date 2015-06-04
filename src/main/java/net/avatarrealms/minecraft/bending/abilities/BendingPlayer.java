package net.avatarrealms.minecraft.bending.abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.event.AbilityCooldownEvent;
import net.avatarrealms.minecraft.bending.utils.PluginTools;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;


public class BendingPlayer {

	private static Map<Abilities, Long> abilityCooldowns = new HashMap<Abilities, Long>();
	private static long globalCooldown = 250;

	private UUID player;
	private String language;

	private Map<Integer, Abilities> slotAbilities = new HashMap<Integer, Abilities>();
	private Map<Material, Abilities> itemAbilities = new HashMap<Material, Abilities>();

	private List<BendingType> bendings = new LinkedList<BendingType>();
	private List<BendingSpecializationType> specializations = new LinkedList<BendingSpecializationType>();

	private Map<Abilities, Long> cooldowns = new HashMap<Abilities, Long>();

	private boolean bendToItem = ConfigManager.bendToItem;

	private long paralyzeTime = 0;
	private long slowTime = 0;

	private long lastTime = 0;

	private boolean tremorsense = true;

	public BendingPlayer (UUID id) {
		this.player = id;
		this.language = PluginTools.getDefaultLanguage();
		this.lastTime = System.currentTimeMillis();
	}

	public BendingPlayer (BendingPlayerData data) {
		this.player = data.getPlayer();

		this.bendings = data.getBendings();
		this.language = data.getLanguage();
		this.bendToItem = data.isBendToItem();
		this.itemAbilities = data.getItemAbilities();
		this.slotAbilities = data.getSlotAbilities();

		this.specializations = data.getSpecialization();

		this.lastTime = data.getLastTime();
	}

	public static BendingPlayer getBendingPlayer (Player player) {
		return Bending.database.get(player.getUniqueId());
	}

	public static void initializeCooldowns () {
		if (abilityCooldowns.isEmpty()) {
			for (Abilities ability : Abilities.values()) {
				long cd = 0;
				switch (ability) {
					case WaterManipulation:
						cd = 1000;
						break;
					case EarthBlast:
						cd = 1000;
						break;
					case EarthGrab:
						cd = ConfigManager.earthGrabCooldown;
						break;
					case AirSwipe:
						cd = ConfigManager.airSwipeCooldown;
						break;
					case HighJump:
						cd = ConfigManager.highJumpCooldown;
						break;
					case RapidPunch:
						cd = ConfigManager.rapidPunchCooldown;
						break;
					case Tremorsense:
						cd = ConfigManager.tremorsenseCooldown;
						break;
					case FireBlast:
						cd = ConfigManager.fireBlastCooldown;
						break;
					case FireJet:
						cd = ConfigManager.fireJetCooldown;
						break;
					case IceSpike:
						cd = ConfigManager.icespikeCooldown;
						break;
					case SmokeBomb:
						cd = ConfigManager.smokeBombCooldown;
						break;
					case PoisonnedDart:
						cd = ConfigManager.poisonnedDartCooldown;
						break;
					case Dash:
						cd = ConfigManager.dashCooldown;
						break;
					case FireBlade:
						cd = ConfigManager.fireBladeCooldown;
						break;
					case Bloodbending:
						cd = ConfigManager.bloodbendingCooldown;
						break;
					case PlasticBomb:
						cd = ConfigManager.plasticCooldown;
						break;
					case AvatarState:
						cd = ConfigManager.avatarstateCooldown;
						System.out.println(cd);
						break;
					default:
						cd = 0;
						break;
				}
				abilityCooldowns.put(ability, cd);
			}
		}
	}

	public boolean isOnGlobalCooldown () {
		return (System.currentTimeMillis() <= (this.lastTime + globalCooldown));
	}

	public boolean isOnCooldown (Abilities ability) {

		if (isOnGlobalCooldown()) {
			return true;
		}

		if (this.cooldowns.containsKey(ability)) {
			double time = System.currentTimeMillis() - this.cooldowns.get(ability);
			return (time <= abilityCooldowns.get(ability));

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
		cooldown(null);
	}

	public void cooldown (Abilities ability) {
		long time = System.currentTimeMillis();
		if (ability != null) {
			this.cooldowns.put(ability, time);
		}
		this.lastTime = time;
		if (ability != null) {
			Bending.callEvent(new AbilityCooldownEvent(this, ability));
		}
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

	public void clearSpecialization () {
		this.specializations.clear();
		Bending.database.save(this.player);
	}

	public void clearAbilities () {
		this.slotAbilities = new HashMap<Integer, Abilities>();
		this.itemAbilities = new HashMap<Material, Abilities>();
		Bending.database.save(this.player);
	}

	public void removeBender () {
		clearAbilities();
		this.specializations.clear();
		this.bendings.clear();
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
		if (this.bendToItem) {
			Material item = player.getItemInHand().getType();
			return getAbility(item);
		}
		else {
			int slot = player.getInventory().getHeldItemSlot();
			return getAbility(slot);
		}
	}

	public Abilities getAbility (int slot) {
		return this.slotAbilities.get(slot);
	}

	public Abilities getAbility (Material item) {
		return this.itemAbilities.get(item);
	}

	public void setAbility (int slot, Abilities ability) {
		this.slotAbilities.put(slot, ability);
		Bending.database.save(this.player);
	}

	public void setAbility (Material item, Abilities ability) {
		this.itemAbilities.put(item, ability);
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
		if (this.bendToItem) {
			Material item = p.getItemInHand().getType();
			removeAbility(item);
		}
		else {
			int slot = p.getInventory().getHeldItemSlot();
			removeAbility(slot);
		}
		Bending.database.save(this.player);
	}

	public void removeAbility (int slot) {
		setAbility(slot, null);
		Bending.database.save(this.player);
	}

	public void removeAbility (Material item) {
		setAbility(item, null);
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

	public void setLanguage (String language) {
		this.language = language;
		Bending.database.save(this.player);
	}

	public String getLanguage () {
		return this.language;
	}

	public void setBendToItem (boolean value) {
		this.bendToItem = value;
		Bending.database.save(this.player);
	}

	public boolean getBendToItem () {
		return this.bendToItem;
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
		string += "Language=" + this.language;
		string += ", ";
		if (ConfigManager.bendToItem) {
			string += "Binds=" + this.itemAbilities;
		}
		else {
			string += "Binds=" + this.slotAbilities;
		}
		string += "}";
		return string;
	}

	public List<BendingSpecializationType> getSpecializations () {
		return this.specializations;
	}

	public BendingPlayerData serialize () {
		BendingPlayerData result = new BendingPlayerData();
		result.setBendings(this.bendings);
		result.setBendToItem(this.bendToItem);
		result.setItemAbilities(this.itemAbilities);
		result.setLanguage(this.language);
		result.setLastTime(this.lastTime);
		result.setSpecialization(this.specializations);
		result.setPlayer(this.player);
		result.setSlotAbilities(this.slotAbilities);

		return result;
	}

}
