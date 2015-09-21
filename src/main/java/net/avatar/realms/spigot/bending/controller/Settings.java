package net.avatar.realms.spigot.bending.controller;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Settings {

	@ConfigurationParameter("abilities.global-cooldown")
	public static long GLOBAL_COOLDOWN = 250;

	@ConfigurationParameter("bending.revert")
	public static boolean REVERSE_BENDING = true;

	@ConfigurationParameter("bending.safe-revert")
	public static boolean SAFE_REVERSE = true;

	@ConfigurationParameter("bending.reverse-time")
	public static long REVERSE_TIME = 120000;

	@ConfigurationParameter("fire.day-factor")
	public static double DAY_FACTOR = 1.1;

	@ConfigurationParameter("water.night-factor")
	public static double NIGHT_FACTOR = 1.1;

	@ConfigurationParameter("earth.bendables")
	public static String[] EARTHBENDABLES = { "STONE", "CLAY", "DIRT", "GRASS", "GRAVEL", "COAL_ORE", "LAPIS_ORE", "REDSTONE_ORE", "SAND", "GLOWING_REDSTONE_ORE", "MYCEL", };

	@ConfigurationParameter("aliases.air")
	public static String[] AIR_ALIASES = { "air", "airbending", "airbender" };

	@ConfigurationParameter("aliases.chi")
	public static String[] CHI_ALIASES = { "chi", "chiblocker", "chiblocking" };

	@ConfigurationParameter("aliases.earth")
	public static String[] EARTH_ALIASES = { "earth", "earthbender", "earthbending" };

	@ConfigurationParameter("aliases.fire")
	public static String[] FIRE_ALIASES = { "fire", "firebender", "firebending" };

	@ConfigurationParameter("aliases.water")
	public static String[] WATER_ALIASES = { "water", "waterbender", "waterbender" };

	@ConfigurationParameter("chi.fall-damage-reduction")
	public static double CHI_FALL_REDUCTION = 1.0;

	@ConfigurationParameter("chi.dodge-chance")
	public static double CHI_DODGE_CHANCE = 10.0;

	@ConfigurationParameter("chi.comboreset")
	public static long CHI_COMBO_RESET = 7500;

	@ConfigurationParameter("server.sea-level")
	public static int SEA_LEVEL = 63;

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

	@ConfigurationParameter("chat-colors.chi")
	public static String CHI_COLOR = "YELLOW";

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

	public static String getColorString(String name) {
		if (name.equals("Energy")) {
			return AVATAR_COLOR;
		} else if (name.equals("ChiBlocker")) {
			return CHI_COLOR;
		} else if (name.equals("Fire")) {
			return FIRE_COLOR;
		} else if (name.equals("Water")) {
			return WATER_COLOR;
		} else if (name.equals("Air")) {
			return AIR_COLOR;
		} else if (name.equals("Earth")) {
			return EARTH_COLOR;
		} else {
			return "WHITE";
		}
	}

	public static List<String> getEarthBendablesBlocksNames() {
		return Arrays.asList(EARTHBENDABLES);
	}

}
