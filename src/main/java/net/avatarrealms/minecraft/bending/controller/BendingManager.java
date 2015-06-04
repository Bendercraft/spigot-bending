package net.avatarrealms.minecraft.bending.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.abilities.BendingType;
import net.avatarrealms.minecraft.bending.abilities.TempPotionEffect;
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
import net.avatarrealms.minecraft.bending.abilities.chi.C4;
import net.avatarrealms.minecraft.bending.abilities.chi.PoisonnedDart;
import net.avatarrealms.minecraft.bending.abilities.chi.RapidPunch;
import net.avatarrealms.minecraft.bending.abilities.chi.SmokeBomb;
import net.avatarrealms.minecraft.bending.abilities.earth.Catapult;
import net.avatarrealms.minecraft.bending.abilities.earth.CompactColumn;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthArmor;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthBlast;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthColumn;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthGrab;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthPassive;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthTunnel;
import net.avatarrealms.minecraft.bending.abilities.earth.LavaTrain;
import net.avatarrealms.minecraft.bending.abilities.earth.MetalBending;
import net.avatarrealms.minecraft.bending.abilities.earth.Ripple;
import net.avatarrealms.minecraft.bending.abilities.earth.Shockwave;
import net.avatarrealms.minecraft.bending.abilities.earth.Tremorsense;
import net.avatarrealms.minecraft.bending.abilities.energy.AvatarState;
import net.avatarrealms.minecraft.bending.abilities.fire.Combustion;
import net.avatarrealms.minecraft.bending.abilities.fire.Cook;
import net.avatarrealms.minecraft.bending.abilities.fire.Enflamed;
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
import net.avatarrealms.minecraft.bending.abilities.water.FastSwimming;
import net.avatarrealms.minecraft.bending.abilities.water.FreezeMelt;
import net.avatarrealms.minecraft.bending.abilities.water.HealingWaters;
import net.avatarrealms.minecraft.bending.abilities.water.IceSpike;
import net.avatarrealms.minecraft.bending.abilities.water.IceSpike2;
import net.avatarrealms.minecraft.bending.abilities.water.OctopusForm;
import net.avatarrealms.minecraft.bending.abilities.water.Plantbending;
import net.avatarrealms.minecraft.bending.abilities.water.Torrent;
import net.avatarrealms.minecraft.bending.abilities.water.TorrentBurst;
import net.avatarrealms.minecraft.bending.abilities.water.WaterManipulation;
import net.avatarrealms.minecraft.bending.abilities.water.WaterReturn;
import net.avatarrealms.minecraft.bending.abilities.water.WaterSpout;
import net.avatarrealms.minecraft.bending.abilities.water.WaterWall;
import net.avatarrealms.minecraft.bending.abilities.water.Wave;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.Metrics;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BendingManager implements Runnable {
	public static List<Player> flyingplayers = new LinkedList<Player>();
	public Bending plugin;
	private long time;
	private List<World> worlds = new LinkedList<World>();
	private Map<World, Boolean> nights = new HashMap<World, Boolean>();
	private Map<World, Boolean> days = new HashMap<World, Boolean>();

	public static final String defaultsunrisemessage = "You feel the strength of the rising sun empowering your firebending.";
	public static final String defaultsunsetmessage = "You feel the empowering of your firebending subside as the sun sets.";
	public static final String defaultmoonrisemessage = "You feel the strength of the rising moon empowering your waterbending.";
	public static final String defaultmoonsetmessage = "You feel the empowering of your waterbending subside as the moon sets.";
	
	public BendingManager(Bending bending) {
		plugin = bending;
		time = System.currentTimeMillis();
	}

	public void run() {
		try {
			Bending.time_step = System.currentTimeMillis() - time;
			time = System.currentTimeMillis();
			
			manageAirbending();
			manageEarthbending();
			manageFirebending();
			manageWaterbending();
			manageChiBlocking();
			// manageMessages();
			TempPotionEffect.progressAll();
			AvatarState.progressAll();
			// handleFlying();
			Flight.handle();
			handleDayNight();
			
			Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("global", "run")), String.valueOf(System.currentTimeMillis() - time));
			Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("global", "step")), String.valueOf(Bending.time_step));
		} catch (Exception e) {
			PluginTools.stopAllBending();
			plugin.getLogger().log(Level.SEVERE, "Exception in bending loop", e);
		}

	}

	private void manageAirbending() {
		long current = System.currentTimeMillis();
		
		long temp = System.currentTimeMillis();
		AirBubble.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("air", "bubbles")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		AirBlast.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("air", "blast")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		AirShield.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("air", "shield")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		AirSuction.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("air", "suction")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		AirSwipe.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("air", "swipe")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		Speed.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("air", "speed")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		Tornado.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("air", "tornado")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		AirBurst.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("air", "burst")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		AirScooter.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("air", "scooter")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		AirSpout.spoutAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("air", "spout")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		Suffocate.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("air", "suffocate")), String.valueOf(System.currentTimeMillis() - temp));
		
		
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("air", "total")), String.valueOf(System.currentTimeMillis() - current));
	}

	private void manageEarthbending() {
		long current = System.currentTimeMillis();
		
		long temp;
		
		temp = System.currentTimeMillis();
		Catapult.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("earth", "catapult")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		EarthColumn.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("earth", "raise")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		CompactColumn.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("earth", "collapse")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		EarthBlast.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("earth", "blast")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		EarthGrab.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("earth", "grab")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		EarthTunnel.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("earth", "tunnel")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		EarthArmor.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("earth", "armor")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		EarthPassive.revertSands();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("earth", "passive")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		Shockwave.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("earth", "shockwave")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		Ripple.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("earth", "ripple")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		Tremorsense.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("earth", "tremorsense")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		LavaTrain.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("earth", "lavatrain")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		MetalBending.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("earth", "metal")), String.valueOf(System.currentTimeMillis() - temp));
		

		temp = System.currentTimeMillis();
		for (Block block : RevertChecker.revertQueue.keySet()) {
			// Tools.removeEarthbendedBlockByIndex(block);
			// if (Tools.revertBlock(block))
			BlockTools.revertBlock(block);
			RevertChecker.revertQueue.remove(block);
		}

		for (int i : RevertChecker.airRevertQueue.keySet()) {
			BlockTools.revertAirBlock(i);
			RevertChecker.airRevertQueue.remove(i);
		}
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("earth", "revert")), String.valueOf(System.currentTimeMillis() - temp));
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("earth", "total")), String.valueOf(System.currentTimeMillis() - current));
	}

	private void manageFirebending() {
		long current = System.currentTimeMillis();
		long temp;
		
		temp = System.currentTimeMillis();
		FireStream.progressAll();
		FireStream.removeAllNoneFireIgnitedBlock();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("fire", "stream")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		FireBall.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("fire", "ball")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		WallOfFire.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("fire", "wall")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		Lightning.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("fire", "lightning")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		FireShield.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("fire", "shield")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		FireProtection.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("fire", "protection")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		FireBlast.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("fire", "blast")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		FireBurst.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("fire", "burst")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		FireJet.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("fire", "jet")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		FireStream.dissipateAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("fire", "stream2")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		Cook.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("fire", "cook")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		Illumination.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("fire", "illumination")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		Enflamed.handleFlames();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("fire", "handleflames")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		FireBlade.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("fire", "blade")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		Combustion.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("fire", "combustion")), String.valueOf(System.currentTimeMillis() - temp));
		
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("fire", "total")), String.valueOf(System.currentTimeMillis() - current));
	}

	private void manageChiBlocking() {
		long current = System.currentTimeMillis();
		
		long temp = System.currentTimeMillis();
		
		RapidPunch.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("chi", "rapid")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		SmokeBomb.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("chi", "smoke")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		C4.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("chi", "C4")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		PoisonnedDart.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("chi", "dart")), String.valueOf(System.currentTimeMillis() - temp));
		
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("chi", "total")), String.valueOf(System.currentTimeMillis() - current));
	}

	private void manageWaterbending() {
		long current = System.currentTimeMillis();
		
		long temp = System.currentTimeMillis();
		FreezeMelt.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("water", "FreezeMelt")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		WaterSpout.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("water", "spout")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		WaterManipulation.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("water", "manip")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		WaterWall.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("water", "wall")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		Wave.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("water", "wave")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		IceSpike.progressAll();
		IceSpike2.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("water", "spike")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		Torrent.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("water", "torrent")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		TorrentBurst.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("water", "tBurst")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		Bloodbending.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("water", "bloodBend")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		HealingWaters.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("water", "heal")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		FastSwimming.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("water", "fSwim")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		OctopusForm.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("water", "octopus")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		Plantbending.regrow();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("water", "plant")), String.valueOf(System.currentTimeMillis() - temp));
		
		temp = System.currentTimeMillis();
		WaterReturn.progressAll();
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("water", "return")), String.valueOf(System.currentTimeMillis() - temp));
	
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("water", "global")), String.valueOf(System.currentTimeMillis() - current));
	}

	private void handleDayNight() {
		long current = System.currentTimeMillis();
		for (World world : plugin.getServer().getWorlds())
			if (world.getWorldType() == WorldType.NORMAL
					&& !worlds.contains(world)) {
				worlds.add(world);
				nights.put(world, false);
				days.put(world, false);
			}

		List<World> removeWorlds = new LinkedList<World>();
		for (World world : worlds) {
			if (!plugin.getServer().getWorlds().contains(world)) {
				removeWorlds.add(world);
				continue;
			}
			boolean night = nights.get(world);
			boolean day = days.get(world);
			if (Tools.isDay(world) && !day) {
				for (Player player : world.getPlayers()) {
					if (EntityTools.isBender(player, BendingType.Fire)
							&& player
									.hasPermission("bending.message.daymessage")) {
						ChatColor color = ChatColor.WHITE;
						color = PluginTools.getColor(ConfigManager.getColor("Fire"));
						player.sendMessage(color
								+ "You feel the strength of the rising sun empowering your firebending.");
					}
				}
				days.put(world, true);
			}

			if (!Tools.isDay(world) && day) {
				for (Player player : world.getPlayers()) {
					if (EntityTools.isBender(player, BendingType.Fire)
							&& player
									.hasPermission("bending.message.daymessage")) {
						ChatColor color = ChatColor.WHITE;
						color = PluginTools.getColor(ConfigManager.getColor("Fire"));
						player.sendMessage(color
								+ "You feel the empowering of your firebending subside as the sun sets.");
					}
				}
				days.put(world, false);
			}

			if (Tools.isNight(world) && !night) {
				for (Player player : world.getPlayers()) {
					if (EntityTools.isBender(player, BendingType.Water)
							&& player
									.hasPermission("bending.message.nightmessage")) {
						ChatColor color = ChatColor.WHITE;
						color = PluginTools.getColor(ConfigManager.getColor("Water"));
						player.sendMessage(color
								+ "You feel the strength of the rising moon empowering your waterbending.");
					}
				}
				nights.put(world, true);
			}

			if (!Tools.isNight(world) && night) {
				for (Player player : world.getPlayers()) {
					if (EntityTools.isBender(player, BendingType.Water)
							&& player
									.hasPermission("bending.message.nightmessage")) {
						ChatColor color = ChatColor.WHITE;
						color = PluginTools.getColor(ConfigManager.getColor("Water"));
						player.sendMessage(color
								+ "You feel the empowering of your waterbending subside as the moon sets.");
					}
				}
				nights.put(world, false);
			}
		}

		for (World world : removeWorlds) {
			worlds.remove(world);
		}
		Metrics.ROOT.put(new LinkedList<String>(Arrays.asList("global", "day&night")), String.valueOf(System.currentTimeMillis() - current));
	}

}
