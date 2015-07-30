package net.avatar.realms.spigot.bending.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.utils.ProtectionManager;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;


public class ConfigManager {

	public static boolean enabled = true;
	public static boolean colors = true;
	public static boolean compatibility = false;
	public static String database = "flatfile"; // "flatfile",
												// "mongodb"

	public static String chat = "<name>: <message>";

	public static Map<String, String> prefixes = new HashMap<String, String>();
	public static Map<String, String> color = new HashMap<String, String>();
	public static List<String> earthbendable = new LinkedList<String>();
	public static Map<String, Boolean> useWeapon = new HashMap<String, Boolean>();

	public static int seaLevel;

	public static long globalCooldown;

	public static long chiblockduration;
	public static double dodgechance;
	public static double punchdamage;
	public static double falldamagereduction;

	public static boolean reverseearthbending;
	public static boolean safeRevert;
	public static long revertchecktime;

	public static boolean useTagAPI;

	private static List<String> defaultearthbendable = new LinkedList<String>();
	static {
		defaultearthbendable.add("STONE");
		defaultearthbendable.add("CLAY");
		defaultearthbendable.add("COAL_ORE");
		defaultearthbendable.add("DIAMOND_ORE");
		defaultearthbendable.add("DIRT");
		defaultearthbendable.add("GOLD_ORE");
		defaultearthbendable.add("EMERALD_ORE");
		defaultearthbendable.add("GRASS");
		defaultearthbendable.add("GRAVEL");
		defaultearthbendable.add("IRON_ORE");
		defaultearthbendable.add("LAPIS_ORE");
		defaultearthbendable.add("REDSTONE_ORE");
		defaultearthbendable.add("SAND");
		defaultearthbendable.add("SANDSTONE");
		defaultearthbendable.add("GLOWING_REDSTONE_ORE");
		defaultearthbendable.add("MYCEL");
	}

	public static long dissipateAfter;
//

	public void load (File file) {
		FileConfiguration config = new YamlConfiguration();
		try {
			if (file.exists()) {
				config.load(file);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}

		database = config.getString("Database", "flatfile");

		// Respect plugins
		ProtectionManager.respectWorldGuard = config.getBoolean("respectWorldGuard");
		ProtectionManager.respectFactions = config.getBoolean("respectFactions");
		ProtectionManager.respectCitizens = config.getBoolean("respectCitizens");

		// Chat:
		enabled = config.getBoolean("Chat.Enabled");
		colors = config.getBoolean("Chat.Colors");
		compatibility = config.getBoolean("Chat.Compatibility");
		chat = config.getString("Chat.Format");

		// Prefix
		prefixes.put("Air", config.getString("Chat.Prefix.Air"));
		prefixes.put("Avatar", config.getString("Chat.Prefix.Avatar"));
		prefixes.put("Fire", config.getString("Chat.Prefix.Fire"));
		prefixes.put("Water", config.getString("Chat.Prefix.Water"));
		prefixes.put("Earth", config.getString("Chat.Prefix.Earth"));
		prefixes.put("ChiBlocker", config.getString("Chat.Prefix.ChiBlocker"));

		// Color
		color.put("Energy", config.getString("Chat.Color.Energy"));
		color.put("Air", config.getString("Chat.Color.Air"));
		color.put("Fire", config.getString("Chat.Color.Fire"));
		color.put("Water", config.getString("Chat.Color.Water"));
		color.put("Earth", config.getString("Chat.Color.Earth"));
		color.put("ChiBlocker", config.getString("Chat.Color.ChiBlocker"));

		// Bending

		// Option
		earthbendable = defaultearthbendable;
		if (config.contains("Bending.Option.EarthBendable")) {
			earthbendable = config.getStringList("Bending.Option.EarthBendable");
		}

		// EarthBendable
		useWeapon.put("Air", config.getBoolean("Bending.Option.Bend-With-Weapon.Air"));
		useWeapon.put("Earth", config.getBoolean("Bending.Option.Bend-With-Weapon.Earth"));
		useWeapon.put("Fire", config.getBoolean("Bending.Option.Bend-With-Weapon.Fire"));
		useWeapon.put("Water", config.getBoolean("Bending.Option.Bend-With-Weapon.Water"));
		useWeapon.put("ChiBlocker", config.getBoolean("Bending.Option.Bend-With-Weapon.ChiBlocker"));

		useTagAPI = config.getBoolean("Bending.Option.Use-TagAPI");
		reverseearthbending = config.getBoolean("Bending.Option.Reverse-Earthbending");
		revertchecktime = config.getLong("Bending.Option.Reverse-Earthbending-Check-Time");
		safeRevert = config.getBoolean("Bending.Option.Safe-Revert");
		dissipateAfter = config.getLong("Bending.Option.Firebending-Dissipate-Time");
		seaLevel = config.getInt("Bending.Option.Sea-Level");

		// Properties
		// ChiBlocker
		chiblockduration = config.getLong("Properties.ChiBlocker.ChiBlock-Duration");
		dodgechance = config.getDouble("Properties.ChiBlocker.Dodge-Chance");
		punchdamage = config.getDouble("Properties.ChiBlocker.Punch-Damage");
		falldamagereduction = config.getDouble("Properties.ChiBlocker.Fall-Damage-Reduction");

		globalCooldown = config.getLong("Properties.GlobalCooldown");


	}

	public static String getColor (String element) {
		return color.get(element);
	}

	public static String getPrefix (String element) {
		return prefixes.get(element);
	}

}
