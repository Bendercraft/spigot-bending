package net.avatarrealms.minecraft.bending.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.TempBlock;
import net.avatarrealms.minecraft.bending.abilities.air.AirBlast;
import net.avatarrealms.minecraft.bending.abilities.air.AirBubble;
import net.avatarrealms.minecraft.bending.abilities.air.AirBurst;
import net.avatarrealms.minecraft.bending.abilities.air.AirScooter;
import net.avatarrealms.minecraft.bending.abilities.air.AirShield;
import net.avatarrealms.minecraft.bending.abilities.air.AirSpout;
import net.avatarrealms.minecraft.bending.abilities.air.AirSuction;
import net.avatarrealms.minecraft.bending.abilities.air.AirSwipe;
import net.avatarrealms.minecraft.bending.abilities.air.Speed;
import net.avatarrealms.minecraft.bending.abilities.air.Suffocate;
import net.avatarrealms.minecraft.bending.abilities.air.Tornado;
import net.avatarrealms.minecraft.bending.abilities.chi.PoisonnedDart;
import net.avatarrealms.minecraft.bending.abilities.chi.RapidPunch;
import net.avatarrealms.minecraft.bending.abilities.chi.SmokeBomb;
import net.avatarrealms.minecraft.bending.abilities.earth.Catapult;
import net.avatarrealms.minecraft.bending.abilities.earth.CompactColumn;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthArmor;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthBlast;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthColumn;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthPassive;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthTunnel;
import net.avatarrealms.minecraft.bending.abilities.earth.LavaTrain;
import net.avatarrealms.minecraft.bending.abilities.earth.Shockwave;
import net.avatarrealms.minecraft.bending.abilities.earth.Tremorsense;
import net.avatarrealms.minecraft.bending.abilities.energy.AstralProjection;
import net.avatarrealms.minecraft.bending.abilities.fire.Combustion;
import net.avatarrealms.minecraft.bending.abilities.fire.Cook;
import net.avatarrealms.minecraft.bending.abilities.fire.FireBlade;
import net.avatarrealms.minecraft.bending.abilities.fire.FireBlast;
import net.avatarrealms.minecraft.bending.abilities.fire.FireBurst;
import net.avatarrealms.minecraft.bending.abilities.fire.FireJet;
import net.avatarrealms.minecraft.bending.abilities.fire.FireProtection;
import net.avatarrealms.minecraft.bending.abilities.fire.FireShield;
import net.avatarrealms.minecraft.bending.abilities.fire.FireStream;
import net.avatarrealms.minecraft.bending.abilities.fire.FireBall;
import net.avatarrealms.minecraft.bending.abilities.fire.Illumination;
import net.avatarrealms.minecraft.bending.abilities.fire.Lightning;
import net.avatarrealms.minecraft.bending.abilities.fire.WallOfFire;
import net.avatarrealms.minecraft.bending.abilities.water.Bloodbending;
import net.avatarrealms.minecraft.bending.abilities.water.FreezeMelt;
import net.avatarrealms.minecraft.bending.abilities.water.IceSpike;
import net.avatarrealms.minecraft.bending.abilities.water.IceSpike2;
import net.avatarrealms.minecraft.bending.abilities.water.OctopusForm;
import net.avatarrealms.minecraft.bending.abilities.water.Plantbending;
import net.avatarrealms.minecraft.bending.abilities.water.WaterManipulation;
import net.avatarrealms.minecraft.bending.abilities.water.WaterReturn;
import net.avatarrealms.minecraft.bending.abilities.water.WaterSpout;
import net.avatarrealms.minecraft.bending.abilities.water.WaterWall;
import net.avatarrealms.minecraft.bending.abilities.water.Wave;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.controller.Flight;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PluginTools {
	
	public static boolean allowharmless = true;
	public static boolean respectWorldGuard = true;
	public static boolean respectFactions = true;
	private static Set<Abilities> harmlessAbilities = new HashSet<Abilities>();
	static { 
		harmlessAbilities.add(Abilities.AirScooter);
		harmlessAbilities.add(Abilities.AirSpout);
		harmlessAbilities.add(Abilities.HealingWaters);
		harmlessAbilities.add(Abilities.HighJump);
		harmlessAbilities.add(Abilities.Illumination);
		harmlessAbilities.add(Abilities.Tremorsense);
		harmlessAbilities.add(Abilities.WaterSpout);
	}
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
	
	public static boolean isHarmlessAbility(Abilities ability) {
		return harmlessAbilities.contains(ability);
	}
	
	public static void stopAllBending() {
		AirBlast.removeAll();
		AirBubble.removeAll();
		AirShield.removeAll();
		AirSuction.removeAll();
		AirScooter.removeAll();
		AirSpout.removeAll();
		AirSwipe.removeAll();
		Speed.removeAll();
		Tornado.removeAll();
		AirBurst.removeAll();
		Suffocate.removeAll();

		Catapult.removeAll();
		CompactColumn.removeAll();
		EarthBlast.removeAll();
		EarthColumn.removeAll();
		EarthPassive.removeAll();
		EarthArmor.removeAll();
		EarthTunnel.removeAll();
		Shockwave.removeAll();
		Tremorsense.removeAll();
		LavaTrain.removeAll();

		FreezeMelt.removeAll();
		IceSpike.removeAll();
		IceSpike2.removeAll();
		WaterManipulation.removeAll();
		WaterSpout.removeAll();
		WaterWall.removeAll();
		Wave.removeAll();
		Plantbending.regrow();
		OctopusForm.removeAll();
		Bloodbending.removeAll();

		FireStream.removeAll();
		FireBall.removeAll();
		WallOfFire.removeAll();
		Lightning.removeAll();
		FireShield.removeAll();
		FireProtection.removeAll();
		FireBlast.removeAll();
		FireBurst.removeAll();
		FireJet.removeAll();
		Cook.removeAll();
		Illumination.removeAll();
		FireBlade.removeAll();
		Combustion.removeAll();

		RapidPunch.removeAll();
		SmokeBomb.removeAll();
		PoisonnedDart.removeAll();
		
		AstralProjection.removeAll();

		// BendingManager.removeFlyers();
		Flight.removeAll();
		WaterReturn.removeAll();
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
		return (ChatColor) colors.get(input.toLowerCase().replace("&", ""));
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
			if (i != suplangs.size() - 1) {
				string = string + ", ";
			}
			languages = languages + string;
		}
		return languages;
	}

	public static String getDefaultLanguage() {
		return Bending.language.getDefaultLanguage();
	}

	public static void sendMessage(Player player, String key) {
		sendMessage(player, ChatColor.WHITE, key);
	}

	public static void sendMessage(Player player, ChatColor color, String key) {
		String message = getMessage(player, key);
		if (player == null) {
			verbose(color + message);
		} else {
			player.sendMessage(color + message);
		}
	}

	public static String getMessage(Player player, String key) {
		String language = getLanguage(player);
		String message = Bending.language.getMessage(language, key);
		return message;
	}

	public static String getLanguage(Player player) {
		String language = getDefaultLanguage();
		if (player != null)
			language = BendingPlayer.getBendingPlayer(player).getLanguage();
		return language;
	}

	public static boolean isLanguageSupported(String language) {
		return (Bending.language.getSupportedLanguages().contains(language
				.toLowerCase()));
	}
	
	public static void printHooks() {
		Plugin wgp = Bukkit.getPluginManager().getPlugin("WorldGuard");
		if (wgp != null) {
			verbose("Recognized WorldGuard...");
			if (respectWorldGuard) {
				verbose("Bending is set to respect WorldGuard's build flags.");
			} else {
				verbose("But Bending is set to ignore WorldGuard's flags.");
			}
		}

		Plugin fcp = Bukkit.getPluginManager().getPlugin("Factions");
		if (fcp != null) {
			verbose("Recognized Factions...");
			if (respectFactions) {
				verbose("Bending is set to respect Factions' claimed lands.");
			} else {
				verbose("But Bending is set to ignore Factions' claimed lands.");
			}
		}
	}
	
	public static <T> void writeToLog(T something) {
		StringBuilder builder = new StringBuilder();
		Date now = new Date();
		builder.append("["+now.toString()+"] ");
		if (something != null) {
			builder.append(something.toString());
		}
		try {
			FileWriter fstream = new FileWriter("bending.log", true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(builder.toString());
			out.newLine();
			out.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

	}
	
	public static double firebendingDayAugment(double value, World world) {
		if (Tools.isDay(world)) {
			return ConfigManager.dayFactor * value;
		}
		return value;
	}

	public static double getFirebendingDayAugment(World world) {
		if (Tools.isDay(world)) {
			return ConfigManager.dayFactor;
		}		
		return 1;
	}

	public static double waterbendingNightAugment(double value, World world) {
		if (Tools.isNight(world)) {
			return ConfigManager.nightFactor * value;
		}
		return value;
	}

	public static double getWaterbendingNightAugment(World world) {
		if (Tools.isNight(world))
			return ConfigManager.nightFactor;
		return 1;
	}
	
}
