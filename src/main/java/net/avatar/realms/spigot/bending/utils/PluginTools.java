package net.avatar.realms.spigot.bending.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.air.AirSpout;
import net.avatar.realms.spigot.bending.abilities.deprecated.TempBlock;
import net.avatar.realms.spigot.bending.abilities.earth.Catapult;
import net.avatar.realms.spigot.bending.abilities.earth.CompactColumn;
import net.avatar.realms.spigot.bending.abilities.earth.EarthArmor;
import net.avatar.realms.spigot.bending.abilities.earth.EarthColumn;
import net.avatar.realms.spigot.bending.abilities.earth.EarthTunnel;
import net.avatar.realms.spigot.bending.abilities.earth.LavaTrain;
import net.avatar.realms.spigot.bending.abilities.earth.Shockwave;
import net.avatar.realms.spigot.bending.abilities.fire.Cook;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlast;
import net.avatar.realms.spigot.bending.abilities.fire.FireBurst;
import net.avatar.realms.spigot.bending.abilities.fire.FireProtection;
import net.avatar.realms.spigot.bending.abilities.fire.FireStream;
import net.avatar.realms.spigot.bending.abilities.water.Bloodbending;
import net.avatar.realms.spigot.bending.abilities.water.FreezeMelt;
import net.avatar.realms.spigot.bending.abilities.water.OctopusForm;
import net.avatar.realms.spigot.bending.abilities.water.WaterSpout;
import net.avatar.realms.spigot.bending.abilities.water.WaterWall;
import net.avatar.realms.spigot.bending.abilities.water.Wave;
import net.avatar.realms.spigot.bending.controller.Settings;

public class PluginTools {

	private static final Map<String, ChatColor> colors;
	static {
		Map<String, ChatColor> tmpMap = new HashMap<String, ChatColor>();
		tmpMap.put("black", ChatColor.BLACK);
		tmpMap.put("0", ChatColor.BLACK);

		tmpMap.put("dark blue", ChatColor.DARK_BLUE);
		tmpMap.put("dark_blue", ChatColor.DARK_BLUE);
		tmpMap.put("1", ChatColor.DARK_BLUE);

		tmpMap.put("dark green", ChatColor.DARK_GREEN);
		tmpMap.put("dark_green", ChatColor.DARK_GREEN);
		tmpMap.put("2", ChatColor.DARK_GREEN);

		tmpMap.put("dark aqua", ChatColor.DARK_AQUA);
		tmpMap.put("dark_aqua", ChatColor.DARK_AQUA);
		tmpMap.put("teal", ChatColor.DARK_AQUA);
		tmpMap.put("3", ChatColor.DARK_AQUA);

		tmpMap.put("dark red", ChatColor.DARK_RED);
		tmpMap.put("dark_red", ChatColor.DARK_RED);
		tmpMap.put("4", ChatColor.DARK_RED);

		tmpMap.put("dark purple", ChatColor.DARK_PURPLE);
		tmpMap.put("dark_purple", ChatColor.DARK_PURPLE);
		tmpMap.put("purple", ChatColor.DARK_PURPLE);
		tmpMap.put("5", ChatColor.DARK_PURPLE);

		tmpMap.put("gold", ChatColor.GOLD);
		tmpMap.put("orange", ChatColor.GOLD);
		tmpMap.put("6", ChatColor.GOLD);

		tmpMap.put("gray", ChatColor.GRAY);
		tmpMap.put("grey", ChatColor.GRAY);
		tmpMap.put("7", ChatColor.GRAY);

		tmpMap.put("dark gray", ChatColor.DARK_GRAY);
		tmpMap.put("dark_gray", ChatColor.DARK_GRAY);
		tmpMap.put("dark grey", ChatColor.DARK_GRAY);
		tmpMap.put("dark_grey", ChatColor.DARK_GRAY);
		tmpMap.put("8", ChatColor.DARK_GRAY);

		tmpMap.put("blue", ChatColor.BLUE);
		tmpMap.put("9", ChatColor.BLUE);

		tmpMap.put("bright green", ChatColor.GREEN);
		tmpMap.put("bright_green", ChatColor.GREEN);
		tmpMap.put("green", ChatColor.GREEN);
		tmpMap.put("a", ChatColor.GREEN);

		tmpMap.put("aqua", ChatColor.AQUA);
		tmpMap.put("b", ChatColor.AQUA);

		tmpMap.put("red", ChatColor.RED);
		tmpMap.put("c", ChatColor.RED);

		tmpMap.put("light purple", ChatColor.LIGHT_PURPLE);
		tmpMap.put("light_purple", ChatColor.LIGHT_PURPLE);
		tmpMap.put("pink", ChatColor.LIGHT_PURPLE);
		tmpMap.put("d", ChatColor.LIGHT_PURPLE);

		tmpMap.put("yellow", ChatColor.YELLOW);
		tmpMap.put("e", ChatColor.YELLOW);

		tmpMap.put("white", ChatColor.WHITE);
		tmpMap.put("f", ChatColor.WHITE);

		tmpMap.put("random", ChatColor.MAGIC);
		tmpMap.put("magic", ChatColor.MAGIC);
		tmpMap.put("k", ChatColor.MAGIC);

		colors = Collections.unmodifiableMap(tmpMap);
	}

	public static void stopAllBending() {

		Catapult.removeAll();
		CompactColumn.removeAll();
		EarthColumn.removeAll();
		EarthArmor.removeAll();
		EarthTunnel.removeAll();
		Shockwave.removeAll();
		LavaTrain.removeAll();

		WaterWall.removeAll();
		Wave.removeAll();
		Bloodbending.removeAll();

		FireStream.removeAll();
		FireProtection.removeAll();
		FireBlast.removeAll();
		FireBurst.removeAll();
		Cook.removeAll();

		// BendingManager.removeFlyers();
		TempBlock.removeAll();
		BlockTools.removeAllEarthbendedBlocks();
	}

	public static void removeSpouts(Location location, double radius,
			Player sourceplayer) {
		WaterSpout.removeSpouts(location, radius, sourceplayer);
		AirSpout.removeSpouts(location, radius, sourceplayer);
	}

	public static void removeSpouts(Location location, Player sourceplayer) {
		removeSpouts(location, 1.5, sourceplayer);
	}

	public static ChatColor getColor(String input) {
		return colors.get(input.toLowerCase().replace("&", ""));
	}

	public static <T> void verbose(T something) {
		if (something != null) {
			Bending.log.info("[Bending] " + something.toString());
		}
	}

	public static String getSupportedLanguages() {
		String languages = "";
		List<String> suplangs = Bending.language.getSupportedLanguages();
		for (int i = 0; i < suplangs.size(); i++) {
			String string = suplangs.get(i);
			if (i != (suplangs.size() - 1)) {
				string = string + ", ";
			}
			languages = languages + string;
		}
		return languages;
	}

	public static String getDefaultLanguage() {
		return Bending.language.getDefaultLanguage();
	}

	public static boolean isLanguageSupported(String language) {
		return (Bending.language.getSupportedLanguages().contains(language
				.toLowerCase()));
	}

	public static double firebendingDayAugment(double value, World world) {
		if (Tools.isDay(world)) {
			return Settings.DAY_FACTOR * value;
		}
		return value;
	}

	public static double getFirebendingDayAugment(World world) {
		if (Tools.isDay(world)) {
			return Settings.DAY_FACTOR;
		}
		return 1;
	}

	public static double waterbendingNightAugment(double value, World world) {
		if (Tools.isNight(world)) {
			return Settings.NIGHT_FACTOR * value;
		}
		return value;
	}

	public static double getWaterbendingNightAugment(World world) {
		if (Tools.isNight(world)) {
			return Settings.NIGHT_FACTOR;
		}
		return 1;
	}

}
