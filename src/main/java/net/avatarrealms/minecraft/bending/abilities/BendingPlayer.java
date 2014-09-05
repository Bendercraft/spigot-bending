package net.avatarrealms.minecraft.bending.abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.avatarrealms.minecraft.bending.controller.BendingPlayers;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class BendingPlayer {

	private static Map<UUID, BendingPlayer> players = new HashMap<UUID, BendingPlayer>();

	private static Map<Abilities, Long> abilityCooldowns = new HashMap<Abilities, Long>();
	private static long globalCooldown = 250;
	private static BendingPlayers config = Tools.config;

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

	public BendingPlayer(UUID player) {
		if (players.containsKey(player)) {
			players.remove(player);
		}

		language = PluginTools.getDefaultLanguage();
		this.player = player;
		lastTime = System.currentTimeMillis();

		players.put(player, this);
	}

	public static List<BendingPlayer> getBendingPlayers() {
		List<BendingPlayer> bPlayers = new ArrayList<BendingPlayer>(
				players.values());
		return bPlayers;
	}

	public static BendingPlayer getBendingPlayer(Player player) {
		if (players.containsKey(player.getUniqueId())) {
			return players.get(player.getUniqueId());
		}

		if (config == null) {
			config = Tools.config;
		}

		BendingPlayer bender = config.getBendingPlayer(player);
		if (bender != null) {
			players.put(player.getUniqueId(), bender);
			return bender;
		} else {
			return new BendingPlayer(player.getUniqueId());
		}
	}

	public static void initializeCooldowns() {
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
					case EarthGrab :
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
					case Dash :
						cd = ConfigManager.dashCooldown;
						break;
					default:
						cd = 0;
						break;
				}
				abilityCooldowns.put(ability, cd);
			}
		}
	}

	public boolean isOnGlobalCooldown() {
		return (System.currentTimeMillis() <= lastTime + globalCooldown);
	}

	public boolean isOnCooldown(Abilities ability) {
		if (ability == Abilities.AvatarState)
			return false;
		if (isOnGlobalCooldown()) {
			return true;
		}

		if (cooldowns.containsKey(ability)) {
			double time = System.currentTimeMillis() - cooldowns.get(ability);
			//PluginTools.verbose(time);
			//PluginTools.verbose(ability + ": " + abilityCooldowns.get(ability));
			return (time <= abilityCooldowns.get(ability));

		} else {
			return false;
		}
	}

	public void toggleTremorsense() {
		tremorsense = !tremorsense;
	}

	public boolean isTremorsensing() {
		return tremorsense;
	}

	public void cooldown() {
		cooldown(null);
	}

	public void cooldown(Abilities ability) {
		long time = System.currentTimeMillis();
		if (ability != null)
			cooldowns.put(ability, time);
		lastTime = time;
	}

	public UUID getPlayerID() {
		return player;
	}

	public boolean isBender() {
		return !bendings.isEmpty();
	}

	public boolean isBender(BendingType type) {
		return bendings.contains(type);
	}
	
	public boolean isSpecialized(BendingSpecializationType specialization) {
		return specializations.contains(specialization);
	}

	public void setBender(BendingType type) {
		removeBender();
		bendings.add(type);
	}

	public void addBender(BendingType type) {
		if (!bendings.contains(type))
			bendings.add(type);
	}
	
	public void setSpecialization(BendingSpecializationType specialization) {
		specializations.clear();
		specializations.add(specialization);
	}
	public void addSpecialization(BendingSpecializationType specialization) {
		if (!specializations.contains(specialization))
			specializations.add(specialization);
	}

	public void clearAbilities() {
		slotAbilities = new HashMap<Integer, Abilities>();
		itemAbilities = new HashMap<Material, Abilities>();
	}

	public void removeBender() {
		bendings.clear();
		specializations.clear();
		clearAbilities();
	}

	public Abilities getAbility() {
		Player player = this.getPlayer();
		if (player == null)
			return null;
		if (!player.isOnline() || player.isDead())
			return null;
		if (bendToItem) {
			Material item = player.getItemInHand().getType();
			return getAbility(item);
		} else {
			int slot = player.getInventory().getHeldItemSlot();
			return getAbility(slot);
		}
	}

	public Abilities getAbility(int slot) {
		return slotAbilities.get(slot);
	}

	public Abilities getAbility(Material item) {
		return itemAbilities.get(item);
	}

	public void setAbility(int slot, Abilities ability) {
		slotAbilities.put(slot, ability);
	}

	public void setAbility(Material item, Abilities ability) {
		itemAbilities.put(item, ability);
	}

	public void removeSelectedAbility() {
		Player player = this.getPlayer();
		if (player == null)
			return;
		if (!player.isOnline() || player.isDead())
			return;
		if (bendToItem) {
			Material item = player.getItemInHand().getType();
			removeAbility(item);
		} else {
			int slot = player.getInventory().getHeldItemSlot();
			removeAbility(slot);
		}
	}

	public void removeAbility(int slot) {
		setAbility(slot, null);
	}

	public void removeAbility(Material item) {
		setAbility(item, null);
	}

	public Player getPlayer() {
		return (Player) EntityTools.getEntityByUUID(this.player);
	}

	public List<BendingType> getBendingTypes() {
		List<BendingType> list = new ArrayList<BendingType>();
		for (BendingType index : bendings) {
			list.add(index);
		}
		return list;
	}
	
	public String bendingsToString() {
		
		Player pl = getPlayer();
		if (pl != null) {
			String str = pl.getName() + " : \n";
			for (BendingType type : bendings) {
				str+=type.toString() + "\n";
			}
			return str;
		}
		return "This player seems not to exist.";
		
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLanguage() {
		return language;
	}

	public void setBendToItem(boolean value) {
		bendToItem = value;
	}

	public boolean getBendToItem() {
		return bendToItem;
	}

	public boolean canBeParalyzed() {
		return (System.currentTimeMillis() > paralyzeTime);
	}

	public boolean canBeSlowed() {
		return (System.currentTimeMillis() > slowTime);
	}

	public void paralyze(long cooldown) {
		paralyzeTime = System.currentTimeMillis() + cooldown;
	}

	public void slow(long cooldown) {
		slowTime = System.currentTimeMillis() + cooldown;
	}

	public long getLastTime() {
		return lastTime;
	}

	public void delete() {
		players.remove(this.player);
	}

	public String toString() {
		String string = "BendingPlayer{";
		string += "Player=" + this.player.toString();
		string += ", ";
		string += "Bendings=" + bendings;
		string += ", ";
		string += "Language=" + language;
		string += ", ";
		if (ConfigManager.bendToItem) {
			string += "Binds=" + itemAbilities;
		} else {
			string += "Binds=" + slotAbilities;
		}
		string += "}";
		return string;
	}

	public BendingPlayer(BendingPlayerData data) {
		this.player = data.getPlayer();
		if (players.containsKey(this.player)) {
			players.remove(this.player);
		}
		
		bendings = data.getBendings();
		language = data.getLanguage();
		bendToItem = data.isBendToItem();
		itemAbilities = data.getItemAbilities();
		slotAbilities = data.getSlotAbilities();
		
		specializations = data.getSpecialization();

		lastTime = data.getLastTime();

		players.put(this.player, this);
	}

	public BendingPlayerData serialize() {
		BendingPlayerData result = new BendingPlayerData();
		result.setBendings(bendings);
		result.setBendToItem(bendToItem);
		result.setItemAbilities(itemAbilities);
		result.setLanguage(language);
		result.setLastTime(lastTime);
		result.setSpecialization(specializations);
		result.setPlayer(this.player);
		result.setSlotAbilities(slotAbilities);

		return result;
	}

	public static BendingPlayer deserialize(BendingPlayerData data) {
		return new BendingPlayer(data);
	}

	public static BendingPlayer valueOf(BendingPlayerData data) {
		return deserialize(data);
	}

}
