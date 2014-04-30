package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

import dataAccess.BendingPlayers;
import dataAccess.ConfigManager;
import dataAccess.CustomSerializable;
import bending.Bending;
import business.Tools;

@SerializableAs("BendingPlayer")
public class BendingPlayer implements CustomSerializable {

	private static ConcurrentHashMap<String, BendingPlayer> players = new ConcurrentHashMap<String, BendingPlayer>();

	private static Map<Abilities, Long> abilityCooldowns = new HashMap<Abilities, Long>();
	private static long globalCooldown = 250;
	private static BendingPlayers config = Tools.config;

	private String playername;
	private String language;

	private List<Integer> slotAbilities = initializeEmptySlots();
	private List<Integer> itemAbilities = initializeEmptyItems();

	private List<Integer> bendingType = new ArrayList<Integer>();

	private Map<Abilities, Long> cooldowns = new HashMap<Abilities, Long>();

	private boolean bendToItem = ConfigManager.bendToItem;

	private long paralyzeTime = 0;
	private long slowTime = 0;

	private long lasttime = 0;

	private boolean permaremoved = false;

	private boolean tremorsense = true;

	private static List<Integer> initializeEmptySlots() {
		Integer[] array = new Integer[10];
		Arrays.fill(array, -1);
		return Arrays.asList(array);
	}

	private static List<Integer> initializeEmptyItems() {
		Integer[] array = new Integer[500];
		Arrays.fill(array, -1);
		return Arrays.asList(array);
	}

	public BendingPlayer(String player) {
		if (players.containsKey(player)) {
			players.remove(player);
		}

		language = Tools.getDefaultLanguage();

		playername = player;

		lasttime = System.currentTimeMillis();

		players.put(player, this);

		// Tools.verbose(playername + " slot size: " + slotAbilities.size());
		// Tools.verbose(playername + " item size: " + itemAbilities.size());
	}

	public static List<BendingPlayer> getBendingPlayers() {
		List<BendingPlayer> bPlayers = new ArrayList<BendingPlayer>(
				players.values());
		return bPlayers;
	}

	public static BendingPlayer getBendingPlayer(OfflinePlayer player) {
		return getBendingPlayer(player.getName());
	}

	public static BendingPlayer getBendingPlayer(String playername) {
		if (players.containsKey(playername)) {
			return players.get(playername);
		}

		if (config == null) {
			config = Tools.config;
		}

		BendingPlayer player = config.getBendingPlayer(playername);
		if (player != null) {
			players.put(playername, player);
			return player;
		} else {
			return new BendingPlayer(playername);
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
					cd = ConfigManager.icespikecooldown;
					break;
				}
				abilityCooldowns.put(ability, cd);
			}
		}
	}

	public boolean isOnGlobalCooldown() {
		return (System.currentTimeMillis() <= lasttime + globalCooldown);
	}

	public boolean isOnCooldown(Abilities ability) {
		if (ability == Abilities.AvatarState)
			return false;
		if (isOnGlobalCooldown()) {
			return true;
		}

		if (cooldowns.containsKey(ability)) {
			double time = System.currentTimeMillis() - cooldowns.get(ability);
			// Tools.verbose(time);
			// Tools.verbose(ability + ": " + abilityCooldowns.get(ability));
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
		lasttime = time;
	}

	public String getName() {
		return playername;
	}

	public boolean isBender() {
		return !bendingType.isEmpty();
	}

	public boolean isBender(BendingType type) {
		// lasttime = System.currentTimeMillis();
		return bendingType.contains(BendingType.getIndex(type));
	}

	public void setBender(BendingType type) {
		removeBender();
		bendingType.add(BendingType.getIndex(type));
	}

	public void addBender(BendingType type) {
		permaremoved = false;
		if (!bendingType.contains(type))
			bendingType.add(BendingType.getIndex(type));
	}

	public void clearAbilities() {
		slotAbilities = initializeEmptySlots();
		itemAbilities = initializeEmptyItems();
	}

	public void removeBender() {
		bendingType.clear();
		clearAbilities();
	}

	public void permaremoveBender() {
		permaremoved = true;
		removeBender();
	}

	public boolean isPermaRemoved() {
		return permaremoved;
	}

	public void setPermaRemoved(boolean value) {
		permaremoved = value;
	}

	public Abilities getAbility() {
		Player player = Bending.plugin.getServer().getPlayerExact(playername);
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
		return Abilities.getAbility(slotAbilities.get(slot));
	}

	public Abilities getAbility(Material item) {
		int id = item.getId();
		if (id > 450) {
			id = id - 2200 + 400;
		}
		return Abilities.getAbility(itemAbilities.get(id));
	}

	public void setAbility(int slot, Abilities ability) {
		slotAbilities.set(slot, Abilities.getIndex(ability));
	}

	public void setAbility(Material item, Abilities ability) {
		int id = item.getId();
		if (id > 450) {
			id = id - 2200 + 400;
		}
		itemAbilities.set(id, Abilities.getIndex(ability));
	}

	public void removeSelectedAbility() {
		Player player = Bending.plugin.getServer().getPlayerExact(playername);
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
		return Bending.plugin.getServer().getPlayerExact(playername);
	}

	// public static ArrayList<BendingPlayer> getBendingPlayers() {
	// ArrayList<BendingPlayer> list = new ArrayList<BendingPlayer>();
	// for (String player : players.keySet()) {
	// list.add(players.get(player));
	// }
	// return list;
	// }

	public List<BendingType> getBendingTypes() {
		List<BendingType> list = new ArrayList<BendingType>();
		for (int index : bendingType) {
			list.add(BendingType.getType(index));
		}
		return list;
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
		return lasttime;
	}

	public void delete() {
		players.remove(playername);
	}

	public String toString() {
		String string = "BendingPlayer{";
		string += "Player=" + playername;
		string += ", ";
		string += "BendingType=" + bendingType;
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

	@SuppressWarnings("unchecked")
	public BendingPlayer(Map<String, Object> map) {
		playername = (String) map.get("PlayerName");

		if (players.containsKey(playername)) {
			players.remove(playername);
		}

		bendingType = (List<Integer>) map.get("BendingTypes");
		language = (String) map.get("Language");
		bendToItem = (Boolean) map.get("BendToItem");
		itemAbilities = (List<Integer>) map.get("ItemAbilities");
		slotAbilities = (List<Integer>) map.get("SlotAbilities");

		permaremoved = (Boolean) map.get("Permaremove");

		lasttime = (Long) map.get("LastTime");

		players.put(playername, this);
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("PlayerName", playername);
		map.put("BendingTypes", bendingType);
		map.put("Language", language);
		map.put("BendToItem", bendToItem);
		map.put("ItemAbilities", itemAbilities);
		map.put("SlotAbilities", slotAbilities);
		map.put("Permaremove", permaremoved);
		map.put("LastTime", lasttime);
		return map;
	}

	public static BendingPlayer deserialize(Map<String, Object> map) {
		return new BendingPlayer(map);
	}

	public static BendingPlayer valueOf(Map<String, Object> map) {
		return deserialize(map);
	}

}
