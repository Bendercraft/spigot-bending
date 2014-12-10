package net.avatarrealms.minecraft.bending.abilities.energy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.controller.DataLocation;
import net.avatarrealms.minecraft.bending.utils.ProtectionManager;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public class AstralProjection {

	private static Map<UUID, Location> previousLoc = new HashMap<UUID, Location>();

	private static final String FILE_NAME = "AstralLocations.json";
	private static ObjectMapper mapper = new ObjectMapper();
	private static File prevLocations = new File(Bending.plugin.getDataFolder(), FILE_NAME);
	static {
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		if (!prevLocations.exists()) {
			try {
				prevLocations.createNewFile();
				FileWriter content = new FileWriter(prevLocations);
				content.write("{}");
				content.close();
			}
			catch (IOException e) {
				Bending.log.warning("Could not create the file current.json");
			}
		}

		readLocations();

	}

	private static Map<Player, AstralProjection> instances = new HashMap<Player, AstralProjection>();

	private Player player;
	private int foodLevel;
	private double healthLevel;
	private Location origin;

	public AstralProjection (Player p, boolean previous) {

		if (instances.containsKey(p)) {
			AstralProjection ap = instances.get(p);
			ap.removeEffect();
			instances.remove(p);
			return;

		}
		if (ProtectionManager.isRegionProtectedFromBending(p, Abilities.AstralProjection, p.getLocation())) {
			return;
		}

		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(p);

		if (bPlayer.isOnCooldown(Abilities.AstralProjection)) {
			return;
		}

		this.player = p;
		this.origin = p.getLocation();
		this.foodLevel = p.getFoodLevel();
		this.healthLevel = p.getHealth();
		instances.put(p, this);

		if (previous) {
			if (previousLoc.containsKey(this.player.getUniqueId())) {
				this.player.teleport(previousLoc.get(this.player.getUniqueId()));
			}
		}
		this.player.setCustomNameVisible(false);

		bPlayer.cooldown(Abilities.AstralProjection);
	}

	public static void progressAll () {
		boolean keep;
		List<Player> toRemove = new LinkedList<Player>();
		for (Player p : instances.keySet()) {
			keep = instances.get(p).progress();
			if (!keep) {
				toRemove.add(p);
				instances.get(p).removeEffect();
			}
		}

		for (Player p : toRemove) {
			instances.remove(p);
		}
	}

	public boolean progress () {
		if (!this.player.isOnline() || this.player.isDead()) {
			return false;
		}

		if (!this.player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
			this.player
					.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15));
		}

		if (!this.player.hasPotionEffect(PotionEffectType.SPEED)) {
			this.player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
		}

		if (!this.player.hasPotionEffect(PotionEffectType.JUMP)) {
			this.player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 3));
		}

		if (this.player.getFoodLevel() < this.foodLevel) {
			this.player.setFoodLevel(this.foodLevel);
		}

		if (this.player.getHealth() != this.healthLevel) {
			this.player.setHealth(this.healthLevel);
		}
		return true;
	}

	public void removeEffect () {
		this.player.setCustomNameVisible(true);
		if (this.player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
			this.player.removePotionEffect(PotionEffectType.INVISIBILITY);
		}

		if (this.player.hasPotionEffect(PotionEffectType.SPEED)) {
			this.player.removePotionEffect(PotionEffectType.SPEED);
		}

		if (this.player.hasPotionEffect(PotionEffectType.JUMP)) {
			this.player.removePotionEffect(PotionEffectType.JUMP);
		}

		previousLoc.put(this.player.getUniqueId(), this.player.getLocation());
		saveLocations();
		this.player.teleport(this.origin);

	}

	public static void removeAll () {
		for (Player p : instances.keySet()) {
			instances.get(p).removeEffect();
		}

		instances.clear();
	}

	public static boolean isAstralProjecting (Player p) {
		return instances.containsKey(p);
	}

	public static AstralProjection getAstralProjection (Player p) {
		return instances.get(p);
	}

	public static void saveLocations () {
		Map<String, DataLocation> tempMap = new HashMap<String, DataLocation>();
		for (UUID id : previousLoc.keySet()) {
			tempMap.put(id.toString(), DataLocation.fromLocation(previousLoc.get(id)));
		}

		try {
			mapper.writeValue(prevLocations, tempMap);
		}
		catch (Exception e) {
			Bending.log.severe("Was not able to write into AstralLocation.json : " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void readLocations () {
		try {
			Map<String, DataLocation> tempMap = mapper
					.readValue(prevLocations, new TypeReference<Map<String, DataLocation>>() {
					});
			previousLoc.clear();
			for (String id : tempMap.keySet()) {
				previousLoc.put(UUID.fromString(id), DataLocation.toLocation(tempMap.get(id)));
			}
		}
		catch (Exception e) {
			Bending.log.severe("Was not able to read from AstralLocation.json : " + e.getMessage());
			e.printStackTrace();
		}
	}

}
