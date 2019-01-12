package net.bendercraft.spigot.bending.abilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.earth.EarthCore;
import net.bendercraft.spigot.bending.abilities.fire.FirePower;
import net.bendercraft.spigot.bending.abilities.water.WaterBalance;
import net.bendercraft.spigot.bending.controller.Settings;
import net.bendercraft.spigot.bending.db.MySQLDB;
import net.bendercraft.spigot.bending.utils.PluginTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;

public class BendingPlayer {
	
	public static final String OBJECTIVE_STATUS = "status";
	
	private static final Map<UUID, BendingPlayer> cache = new HashMap<>();

	private UUID player;

	private String currentDeck;
	private Map<String, Map<Integer, String>> decks;

	private List<BendingElement> bendings;
	private List<BendingAffinity> affinities;
	private Map<String, BendingPerk> perks;
	private List<String> abilities = new LinkedList<>();

	private Map<String, BendingAbilityCooldown> cooldowns = new HashMap<>();

	private long paralyzeTime = 0;
	private long slowTime = 0;

	private long lastTime;
	
	private boolean usingScoreboard;
	
	private Scoreboard scoreboard;
	
	public FirePower fire = new FirePower();
	public WaterBalance water = new WaterBalance(this);
	public EarthCore earth = new EarthCore(this);
	
	private BendingPlayer(BendingPlayerData data) {
		this.player = data.getPlayer();

		this.bendings = data.getBendings();
		this.affinities = data.getAffinities();
		this.perks = new HashMap<>();
		for(BendingPerk perk : data.getPerks()) {
			this.perks.put(perk.name, perk);
		}
		this.applyPerks();

		this.decks = data.getDecks();
		this.currentDeck = data.getCurrentDeck();
		
		if(this.decks.isEmpty()) {
			this.decks.put("default", new HashMap<>());
		}
		if(this.currentDeck == null) {
			this.currentDeck = decks.keySet().iterator().next();
		}
		
		for(String ability : data.getAbilities()) {
			this.abilities.add(ability.toLowerCase());
		}

		this.lastTime = System.currentTimeMillis();
		
		this.usingScoreboard = true;
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
		MySQLDB.save(player, serialize());
	}

	public static BendingPlayer getBendingPlayer(Player player) {
		return getBendingPlayer(player.getUniqueId());
	}
	
	public static BendingPlayer getBendingPlayer(UUID player) {
		BendingPlayer bender = cache.get(player);
		if(bender == null) {
			MySQLDB.fetch(player);
		}
		return bender;
	}
	
	public static void insert(BendingPlayerData data) {
		if(!cache.containsKey(data.getPlayer())) {
			BendingPlayer bender = new BendingPlayer(data);
			cache.put(data.getPlayer(), bender);
			
			bender.loadScoreboard();
			
			List<BendingElement> els = bender.getBendingTypes();
			ChatColor color = ChatColor.WHITE;
			if ((els != null) && !els.isEmpty()) {
				color  = PluginTools.getColor(Settings.getColor(els.get(0)));
			}
			bender.getPlayer().setDisplayName("<" + color + bender.getPlayer().getName() + ChatColor.WHITE + ">");
			
		}
	}
	
	public static void update(BendingPlayerData data) {
		BendingPlayer bender = getBendingPlayer(data.getPlayer());
		if(bender == null) {
			return;
		}
		Collection<BendingPerk> olds = bender.perks.values();
		bender.perks = new HashMap<>();
		for(BendingPerk perk : data.getPerks()) {
			bender.perks.put(perk.name, perk);
		}
		Collection<BendingPerk> news = bender.perks.values();
		
		bender.applyPerks();
		
		if(olds.size() != news.size() || !olds.containsAll(news)) {
			// Perks updated
			bender.getPlayer().sendMessage(ChatColor.AQUA+"["+ChatColor.GOLD+"Bending"+ChatColor.AQUA+"]"+ChatColor.WHITE+" Your perks have been "+ChatColor.GREEN+"updated"+ChatColor.WHITE+".");
		}
	}
	
	public void applyPerks() {
		// Check max health, and adjust if needed
		int bonus = 0;
		if(hasPerk(BendingPerk.EARTH_INNERCORE)) {
			bonus += 4;
		}
		if(hasPerk(BendingPerk.MASTER_DISENGAGE_PARAPARASTICK_CONSTITUTION)
				&& hasAffinity(BendingAffinity.SWORD)) {
			bonus += 2;
		}
		if(hasPerk(BendingPerk.MASTER_SNIPE_PERSIST_CONSTITUTION)
				&& hasAffinity(BendingAffinity.SWORD)) {
			bonus += 2;
		}
		Player p = getPlayer();
		if(p != null) {
			AttributeInstance attr = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
			attr.setBaseValue(20 + bonus);
		}
	}
	
	public List<BendingPerk> getPerks() {
		return new LinkedList<>(this.perks.values());
	}

	public boolean isOnGlobalCooldown() {
		return System.currentTimeMillis() <= (this.lastTime + Settings.GLOBAL_COOLDOWN);
	}

	public boolean isOnCooldown(String ability) {
		if (isOnGlobalCooldown()) {
			return true;
		}

		if (this.cooldowns.containsKey(ability)) {
			return cooldowns.get(ability).timeLeft(System.currentTimeMillis()) > 0;
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
		long now = System.currentTimeMillis();
		if (ability != null) {
			BendingAbilityCooldown cd = new BendingAbilityCooldown(ability, now, cooldownTime);
			this.cooldowns.put(ability, cd);
		}
		this.lastTime = now;
	}

	public Map<String, BendingAbilityCooldown> getCooldowns() {
		long now = System.currentTimeMillis();
		
		List<String> toRemove = new LinkedList<>();
		for(Entry<String, BendingAbilityCooldown> entry : cooldowns.entrySet()) {
			if(entry.getValue().timeLeft(now) <= 0) {
				toRemove.add(entry.getKey());
			}
		}
		for(String ability : toRemove) {
			cooldowns.remove(ability);
		}
		
		return cooldowns;
	}

	public UUID getPlayerID() {
		return this.player;
	}

	public boolean isBender() {
		return !this.bendings.isEmpty();
	}

	public boolean isBender(BendingElement type) {
		if (type == BendingElement.ENERGY) {
			return true;
		}
		return this.bendings.contains(type);
	}

	public boolean hasAffinity(BendingElement element) {
		return this.affinities.stream().anyMatch(a -> a.getElement() == element);
	}
	
	public boolean hasAffinity(BendingAffinity affinity) {
		return this.affinities.contains(affinity);
	}

	public boolean hasPerk(BendingPerk perk) {
		if (this.perks == null) {
			return false;
		}
		Player p = getPlayer();
		if(p == null) {
			return false;
		}
		if(ProtectionManager.isLocationProtectedFromPerks(p, p.getLocation())) {
			return false;
		}
		return perks.containsKey(perk.name.toLowerCase());
	}

	public void setBender(BendingElement type) {
		removeBender();
		bendings.add(type);
		MySQLDB.save(player, serialize());
	}

	public void addBender(BendingElement type) {
		if (!bendings.contains(type)) {
			bendings.add(type);
			MySQLDB.save(player, serialize());
		}
	}

	public void setAffinity(BendingAffinity affinity) {
		clearAffinity(affinity.getElement());
		affinities.add(affinity);
		MySQLDB.save(player, serialize());
	}

	public void addAffinity(BendingAffinity affinity) {
		if (!affinities.contains(affinity)) {
			affinities.add(affinity);
			MySQLDB.save(player, serialize());
		}
	}

	public void removeAffinity(BendingAffinity affinity) {
		this.affinities.remove(affinity);
		clearAbilities();
		// clear abilities will save for us
	}

	public void clearAffinity(BendingElement element) {
		List<BendingAffinity> toRemove = new LinkedList<>();
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

	public void clearAffinities() {
		affinities.clear();
		MySQLDB.save(player, serialize());
	}

	public void clearAbilities() {
		decks.put(currentDeck, new HashMap<>());
		MySQLDB.save(player, serialize());
	}

	public void removeBender() {
		clearAbilities();
		resetPerks();
		affinities.clear();
		bendings.clear();
		abilities.clear();
		MySQLDB.save(player, serialize());
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
	
	public void loadScoreboard() {
		if(isUsingScoreboard() && scoreboard == null) {
			ScoreboardManager manager = Bukkit.getScoreboardManager();
			scoreboard = manager.getNewScoreboard();
			scoreboard.registerNewTeam(getPlayer().getName());
		    Objective objectiveStatus = scoreboard.registerNewObjective(OBJECTIVE_STATUS, "dummy", "bending");
		    objectiveStatus.setDisplaySlot(DisplaySlot.SIDEBAR);
		    objectiveStatus.setDisplayName("Status");
		    getPlayer().setScoreboard(scoreboard);
		}
	}
	
	public void conflictScoreboard() {
		getPlayer().sendMessage(ChatColor.RED+"[Bending] Scoreboard conflict detected, disabling cooldown GUI (sorry).");
		Bending.getInstance().getLogger().warning("Player "+getPlayer().getName()+" had conflicting scoreboard.");
		setUsingScoreboard(false);
		if(scoreboard != null) {
			scoreboard.getTeams().forEach(Team::unregister);
			scoreboard.getObjectives().forEach(Objective::unregister);
			scoreboard = null;
		}
	}
	
	public void unloadScoreboard() {
		if(!isUsingScoreboard() && scoreboard != null) {
			scoreboard.getTeams().forEach(Team::unregister);
			scoreboard.getObjectives().forEach(Objective::unregister);
			ScoreboardManager manager = Bukkit.getScoreboardManager();
			getPlayer().setScoreboard(manager.getNewScoreboard());
			scoreboard = null;
		}
	}
	
	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	public void bind(int slot, String ability) {
		if(!decks.containsKey(currentDeck)) {
			Bending.getInstance().getLogger().warning("Player "+player+" tried to bind an ability on unknown deck "+currentDeck);
			return;
		}
		decks.get(currentDeck).put(slot, ability);
		MySQLDB.save(player, serialize());
	}

	public void unbind(int slot) {
		bind(slot, null);
		MySQLDB.save(player, serialize());
	}

	public Player getPlayer() {
		return Bukkit.getServer().getPlayer(this.player);
	}

	public List<BendingElement> getBendingTypes() {
		return new ArrayList<>(this.bendings);
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

	public boolean isUsingScoreboard() {
		return usingScoreboard;
	}

	public void setUsingScoreboard(boolean usingScoreboard) {
		this.usingScoreboard = usingScoreboard;
	}
	
	public void refreshPerks() {
		MySQLDB.update(player);
	}

	public void resetPerks() {
		perks.clear();
		MySQLDB.clearPerks(player);
	}
	
	public void addAbility(RegisteredAbility ability) {
		if(!hasAbility(ability)) {
			this.abilities.add(ability.getName().toLowerCase());
			MySQLDB.save(player, serialize());
		}
	}
	
	public void removeAbility(RegisteredAbility ability) {
		if(hasAbility(ability)) {
			this.abilities.remove(ability.getName().toLowerCase());
			MySQLDB.save(player, serialize());
		}
	}

	public boolean hasAbility(String name) {
		return abilities.contains(name.toLowerCase());
	}

	public boolean hasAbility(RegisteredAbility ability) {
		return hasAbility(ability.getName());
	}
	
	public BendingPlayerData serialize() {
		BendingPlayerData result = new BendingPlayerData();
		result.setBendings(new LinkedList<>(this.bendings));
		result.setAffinities(new LinkedList<>(this.affinities));
		result.setPlayer(this.player);
		result.setDecks(this.decks);
		result.setPerks(new LinkedList<>(this.perks.values()));
		result.setAbilities(new LinkedList<>(this.abilities));
		result.setCurrentDeck(this.currentDeck);
		return result;
	}
}
