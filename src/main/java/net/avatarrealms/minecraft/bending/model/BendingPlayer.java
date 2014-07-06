package net.avatarrealms.minecraft.bending.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import net.avatarrealms.minecraft.bending.controller.BendingPlayers;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.data.BendingLevelData;
import net.avatarrealms.minecraft.bending.model.data.BendingPlayerData;
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

	private Map<BendingType, BendingLevel> bendings = new HashMap<BendingType,BendingLevel>();

	private Map<Abilities, Long> cooldowns = new HashMap<Abilities, Long>();

	private boolean bendToItem = ConfigManager.bendToItem;

	private long paralyzeTime = 0;
	private long slowTime = 0;

	private long lasttime = 0;

	private boolean permaremoved = false;

	private boolean tremorsense = true;

	public BendingPlayer(UUID player) {
		if (players.containsKey(player)) {
			players.remove(player);
		}

		language = PluginTools.getDefaultLanguage();
		this.player = player;
		lasttime = System.currentTimeMillis();

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
						//TODO Throw exception here
						cd = 0;
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
		lasttime = time;
	}

	public UUID getPlayerID() {
		return player;
	}

	public boolean isBender() {
		return !bendings.isEmpty();
	}

	public boolean isBender(BendingType type) {
		// lasttime = System.currentTimeMillis();
		return bendings.containsKey(type);
	}
	
	public boolean hasLevel(String ability) {
		
		if (bendings == null) {
			return false;
		}
		
		if (ability.equalsIgnoreCase("plantbending")) {
			if (!bendings.containsKey(BendingType.Water)) {
				return false;
			}
			if (bendings.get(BendingType.Water).getLevel() < ConfigManager.plantbendingLevelRequired){
				return false;
			}
			
			return true;
		}
		
		return true;
	}
	
	public boolean hasLevel(Abilities ability) {
		if (bendings == null) {
			return false;
		}
		
		if (Abilities.isAirbending(ability)) {
			if (!bendings.containsKey(BendingType.Air)) {
				return false;
			}
			
			if (bendings.get(BendingType.Air).getLevel() < ConfigManager.getLevelRequired(ability)){
				return false;
			}
			return true;
		}
		
		if (Abilities.isEarthbending(ability)) {
			if (!bendings.containsKey(BendingType.Earth)) {
				return false;
			}
			if (bendings.get(BendingType.Earth).getLevel() < ConfigManager.getLevelRequired(ability)){
				return false;
			}
			return true;
		}
		
		if (Abilities.isFirebending(ability)) {
			if (!bendings.containsKey(BendingType.Fire)) {
				return false;
			}
			if (bendings.get(BendingType.Fire).getLevel() < ConfigManager.getLevelRequired(ability)){
				return false;
			}
			
			return true;
		}
		
		if (Abilities.isWaterbending(ability)){
			if (!bendings.containsKey(BendingType.Water)) {
				return false;
			}
			if (bendings.get(BendingType.Water).getLevel() < ConfigManager.getLevelRequired(ability)){
				return false;
			}
			
			return true;
		}
		
		if (Abilities.isChiBlocking(ability)) {
			if (!bendings.containsKey(BendingType.ChiBlocker)) {
				return false;
			}
			if (bendings.get(BendingType.ChiBlocker).getLevel() < ConfigManager.getLevelRequired(ability)){
				return false;
			}
			return true;
		}
		
		
		return true;
	}

	public void setBender(BendingType type) {
		removeBender();
		bendings.put(type, new BendingLevel (type, this));
	}

	public void addBender(BendingType type) {
		permaremoved = false;
		if (!bendings.containsKey(type))
			bendings.put(type, new BendingLevel(type,this));
	}

	public void clearAbilities() {
		slotAbilities = new HashMap<Integer, Abilities>();
		itemAbilities = new HashMap<Material, Abilities>();
	}

	public void removeBender() {
		bendings.clear();
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
		for (BendingType index : bendings.keySet()) {
			list.add(index);
		}
		return list;
	}
	
	public String bendingsToString() {
		String str = ""; // TODO : Add the nickname
		for (BendingType type : bendings.keySet()) {
			str+=bendings.get(type).toString()+"\n";
		}
		return str;
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
		
		List<BendingLevelData> bendingData = data.getBendings();
		
		for (BendingLevelData bend: bendingData) {
			BendingLevel bendingLevel = BendingLevel.deserialize(bend);
			bendings.put(bend.getBendingType(), bendingLevel);
			bendingLevel.setBendingPlayer(this);
		}
		language = data.getLanguage();
		bendToItem = data.isBendToItem();
		itemAbilities = data.getItemAbilities();
		slotAbilities = data.getSlotAbilities();

		permaremoved = data.isPermaRemoved();

		lasttime = data.getLastTime();

		players.put(this.player, this);
	}

	public BendingPlayerData serialize() {
		BendingPlayerData result = new BendingPlayerData();
		List<BendingLevelData> bending = new ArrayList<BendingLevelData>();
		for (BendingType bLev : bendings.keySet()) {
			bending.add(bendings.get(bLev).serialize());
		}
		result.setBendings(bending);
		result.setBendToItem(bendToItem);
		result.setItemAbilities(itemAbilities);
		result.setLanguage(language);
		result.setLastTime(lasttime);
		result.setPermaRemoved(permaremoved);
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
	
	public void resetXP() {	
		for (BendingType type : bendings.keySet()) {
			bendings.get(type).setXP(bendings.get(type).getXP()*0.95);
		}
	}
	
	public double getCriticalHit(BendingType type, double damage){
		double newDamage = damage;
		int level = bendings.get(type).getLevel();
		double prc = ((level)/(level+2))*0.4;
		
		Random rand = new Random();
		
		if (rand.nextDouble() < prc) {
			newDamage += 1;
			
			if (level >= 25) {
				prc = ((level)/(level+2))*0.2;
				if (rand.nextDouble() < prc) {
					newDamage += 1;
				}
			}		
		}
		
		return newDamage;
	}
	
	public void earnXP(BendingType type, IAbility ability) {
		bendings.get(type).earnXP(ability);
	}
	
	public Integer getLevel (BendingType type) {
		BendingLevel bLvl = bendings.get(type);
		if (bLvl == null) {
			return 0;
		}
		else {
			return bLvl.getLevel();
		}
	}
	
	public void receiveXP(BendingType type, Integer amount) {
		bendings.get(type).giveXP(amount);
	}
	
	public void setBendingLevel(BendingType type, Integer level) {
		if(bendings.containsKey(type)) {
			bendings.get(type).setLevel(level);
		}
		
	}
	
	public long getLastTime(BendingType type) {
		return bendings.get(type).getLastTime();
	}
	
	public double getSpamHistory(BendingType type) {
		return bendings.get(type).getSpamHistory();
	}
	
	public double getDegressionFactor(BendingType type) {
		return bendings.get(type).getDegressFactor();
	}
	
	public int getMaxLevel() {
		int max = 0;
		for (BendingType type : bendings.keySet()) {
			if (bendings.get(type).getLevel() > max) {
				max = bendings.get(type).getLevel();
			}
		}	
		return max;
	}

}
