package net.bendercraft.spigot.bending.controller;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.bendercraft.spigot.bending.abilities.BendingElement;

public class Settings {
	
	@ConfigurationParameter("database.host")
	public static String DATABASE_HOST = "localhost";
	
	@ConfigurationParameter("database.port")
	public static int DATABASE_PORT = 3306;
	
	@ConfigurationParameter("database.db")
	public static String DATABASE_DB = "changeme_db";
	
	@ConfigurationParameter("database.user")
	public static String DATABASE_USER = "changeme_user";
	
	@ConfigurationParameter("database.password")
	public static String DATABASE_PASSWORD = "changeme_password";
	
	@ConfigurationParameter("abilities.global-cooldown")
	public static long GLOBAL_COOLDOWN = 250;
	
	@ConfigurationParameter("bending.revert")
	public static boolean REVERSE_BENDING = true;
	
	@ConfigurationParameter("bending.safe-revert")
	public static boolean SAFE_REVERSE = true;
	
	@ConfigurationParameter("bending.reverse-time")
	public static long REVERSE_TIME = 120000;
	
	@ConfigurationParameter("earth.bendables")
	public static String[] EARTHBENDABLES = { "STONE", "CLAY", "DIRT", "GRASS", "GRAVEL", "COAL_ORE", "LAPIS_ORE", "REDSTONE_ORE", "SAND", "GLOWING_REDSTONE_ORE", "MYCEL", };
	
	@ConfigurationParameter("aliases.air")
	public static String[] AIR_ALIASES = { "air", "airbending", "airbender" };
	
	@ConfigurationParameter("aliases.master")
	public static String[] MASTER_ALIASES = { "master" };
	
	@ConfigurationParameter("aliases.earth")
	public static String[] EARTH_ALIASES = { "earth", "earthbender", "earthbending" };
	
	@ConfigurationParameter("aliases.fire")
	public static String[] FIRE_ALIASES = { "fire", "firebender", "firebending" };
	
	@ConfigurationParameter("aliases.water")
	public static String[] WATER_ALIASES = { "water", "waterbender", "waterbender" };
	
	@ConfigurationParameter("master.fall-damage-reduction")
	public static double MASTER_FALL_REDUCTION = 1.0;
	
	@ConfigurationParameter("server.respect-worldguard")
	public static boolean RESPECT_WORLDGUARD = true;
	
	@ConfigurationParameter("server.respect-factions")
	public static boolean RESPECT_FACTIONS = false;
	
	@ConfigurationParameter("server.respect-citizens")
	public static boolean RESPECT_CITIZENS = true;
	
	@ConfigurationParameter("server.database")
	public static String DATABASE = "flatfile";
	
	@ConfigurationParameter("chat.enabled")
	public static boolean CHAT_ENABLED = true;
	
	@ConfigurationParameter("chat.colored")
	public static boolean CHAT_COLORED = true;
	
	@ConfigurationParameter("chat.compatibility")
	public static boolean CHAT_COMPATIBILITY = true;
	
	@ConfigurationParameter("chat.format")
	public static String CHAT_FORMAT = "<name>: <message>";
	
	@ConfigurationParameter("chat-colors.energy")
	public static String AVATAR_COLOR = "GOLD";
	
	@ConfigurationParameter("chat-colors.air")
	public static String AIR_COLOR = "AQUA";
	
	@ConfigurationParameter("chat-colors.fire")
	public static String FIRE_COLOR = "RED";
	
	@ConfigurationParameter("chat-colors.earth")
	public static String EARTH_COLOR = "GREEN";
	
	@ConfigurationParameter("chat-colors.water")
	public static String WATER_COLOR = "BLUE";
	
	@ConfigurationParameter("chat-colors.master")
	public static String MASTER_COLOR = "YELLOW";
	
	@ConfigurationParameter("bending.max-decks-amount")
	public static int MAX_DECKS_AMOUNT = 5;
	
	@ConfigurationParameter("bending.deny-items")
	public static boolean DENY_ITEMS = true;
	
	@ConfigurationParameter("bending.enderpearl-cooldown")
	public static long ENDERPEARL_COOLDOWN = 20000;
	
	@ConfigurationParameter("bending.use-scoreboard")
	public static boolean USE_SCOREBOARD = true;
	
	
	public static void applyConfiguration(File configDir) {
		configDir.mkdirs();
		File configFile = new File(configDir, "settings.json");
		
		Map<String, Field> fields = new TreeMap<String, Field>();
		for (Field f : Settings.class.getDeclaredFields()) {
			if (Modifier.isStatic(f.getModifiers())) {
				ConfigurationParameter an = f.getAnnotation(ConfigurationParameter.class);
				if (an != null) {
					fields.put(an.value().toLowerCase(), f);
				}
			}
		}
		ConfigurationManager.applyConfiguration(configFile, fields);
	}
	
	public static String getColor(BendingElement name) {
		if (name == BendingElement.ENERGY) {
			return AVATAR_COLOR;
		} else if (name == BendingElement.MASTER) {
			return MASTER_COLOR;
		} else if (name == BendingElement.FIRE) {
			return FIRE_COLOR;
		} else if (name == BendingElement.WATER) {
			return WATER_COLOR;
		} else if (name == BendingElement.AIR) {
			return AIR_COLOR;
		} else if (name == BendingElement.EARTH) {
			return EARTH_COLOR;
		} else {
			return "WHITE";
		}
	}
	
	public static List<String> getEarthBendablesBlocksNames() {
		return Arrays.asList(EARTHBENDABLES);
	}
	
}
