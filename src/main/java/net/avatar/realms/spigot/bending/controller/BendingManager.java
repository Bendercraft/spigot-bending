package net.avatar.realms.spigot.bending.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.TempPotionEffect;
import net.avatar.realms.spigot.bending.abilities.air.AirBubble;
import net.avatar.realms.spigot.bending.abilities.air.AirBurst;
import net.avatar.realms.spigot.bending.abilities.air.AirScooter;
import net.avatar.realms.spigot.bending.abilities.air.AirShield;
import net.avatar.realms.spigot.bending.abilities.air.AirSpout;
import net.avatar.realms.spigot.bending.abilities.air.AirSuction;
import net.avatar.realms.spigot.bending.abilities.air.AirSwipe;
import net.avatar.realms.spigot.bending.abilities.air.Speed;
import net.avatar.realms.spigot.bending.abilities.air.Suffocate;
import net.avatar.realms.spigot.bending.abilities.air.Tornado;
import net.avatar.realms.spigot.bending.abilities.chi.C4;
import net.avatar.realms.spigot.bending.abilities.chi.RapidPunch;
import net.avatar.realms.spigot.bending.abilities.earth.Catapult;
import net.avatar.realms.spigot.bending.abilities.earth.CompactColumn;
import net.avatar.realms.spigot.bending.abilities.earth.EarthArmor;
import net.avatar.realms.spigot.bending.abilities.earth.EarthBlast;
import net.avatar.realms.spigot.bending.abilities.earth.EarthColumn;
import net.avatar.realms.spigot.bending.abilities.earth.EarthGrab;
import net.avatar.realms.spigot.bending.abilities.earth.EarthPassive;
import net.avatar.realms.spigot.bending.abilities.earth.EarthTunnel;
import net.avatar.realms.spigot.bending.abilities.earth.LavaTrain;
import net.avatar.realms.spigot.bending.abilities.earth.MetalBending;
import net.avatar.realms.spigot.bending.abilities.earth.Ripple;
import net.avatar.realms.spigot.bending.abilities.earth.Shockwave;
import net.avatar.realms.spigot.bending.abilities.earth.Tremorsense;
import net.avatar.realms.spigot.bending.abilities.fire.Combustion;
import net.avatar.realms.spigot.bending.abilities.fire.Cook;
import net.avatar.realms.spigot.bending.abilities.fire.Enflamed;
import net.avatar.realms.spigot.bending.abilities.fire.FireBall;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlade;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlast;
import net.avatar.realms.spigot.bending.abilities.fire.FireBurst;
import net.avatar.realms.spigot.bending.abilities.fire.FireJet;
import net.avatar.realms.spigot.bending.abilities.fire.FireProtection;
import net.avatar.realms.spigot.bending.abilities.fire.FireShield;
import net.avatar.realms.spigot.bending.abilities.fire.FireStream;
import net.avatar.realms.spigot.bending.abilities.fire.Illumination;
import net.avatar.realms.spigot.bending.abilities.fire.Lightning;
import net.avatar.realms.spigot.bending.abilities.fire.WallOfFire;
import net.avatar.realms.spigot.bending.abilities.water.Bloodbending;
import net.avatar.realms.spigot.bending.abilities.water.FastSwimming;
import net.avatar.realms.spigot.bending.abilities.water.FreezeMelt;
import net.avatar.realms.spigot.bending.abilities.water.HealingWaters;
import net.avatar.realms.spigot.bending.abilities.water.IceSpike;
import net.avatar.realms.spigot.bending.abilities.water.IceSpike2;
import net.avatar.realms.spigot.bending.abilities.water.OctopusForm;
import net.avatar.realms.spigot.bending.abilities.water.Plantbending;
import net.avatar.realms.spigot.bending.abilities.water.Torrent;
import net.avatar.realms.spigot.bending.abilities.water.TorrentBurst;
import net.avatar.realms.spigot.bending.abilities.water.WaterManipulation;
import net.avatar.realms.spigot.bending.abilities.water.WaterReturn;
import net.avatar.realms.spigot.bending.abilities.water.WaterSpout;
import net.avatar.realms.spigot.bending.abilities.water.WaterWall;
import net.avatar.realms.spigot.bending.abilities.water.Wave;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.Tools;

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
			
			AbilityManager.getManager().progressAllAbilities();
			
			manageAirbending();
			manageEarthbending();
			manageFirebending();
			manageWaterbending();
			manageChiBlocking();
			// manageMessages();
			TempPotionEffect.progressAll();
			// handleFlying();
			Flight.handle();
			handleDayNight();
		} catch (Exception e) {
			PluginTools.stopAllBending();
			plugin.getLogger().log(Level.SEVERE, "Exception in bending loop", e);
		}

	}

	private void manageAirbending() {
		AirBubble.progressAll();
		AirShield.progressAll();
		AirSuction.progressAll();
		AirSwipe.progressAll();
		Speed.progressAll();
		Tornado.progressAll();
		AirBurst.progressAll();
		AirScooter.progressAll();
		AirSpout.spoutAll();
		Suffocate.progressAll();
	}

	private void manageEarthbending() {
		Catapult.progressAll();
		EarthColumn.progressAll();
		CompactColumn.progressAll();
		EarthBlast.progressAll();
		EarthGrab.progressAll();
		EarthTunnel.progressAll();
		EarthArmor.progressAll();
		EarthPassive.revertSands();
		Shockwave.progressAll();
		Ripple.progressAll();
		Tremorsense.progressAll();
		LavaTrain.progressAll();
		MetalBending.progressAll();
		

		
		Set<Block> copy = new HashSet<Block>(RevertChecker.revertQueue.keySet());
		for (Block block : copy) {
			// Tools.removeEarthbendedBlockByIndex(block);
			// if (Tools.revertBlock(block))
			BlockTools.revertBlock(block);
		}
		RevertChecker.revertQueue.clear();

		Set<Integer> otherCopy = new HashSet<Integer>(RevertChecker.airRevertQueue.keySet());
		for (int i : otherCopy) {
			BlockTools.revertAirBlock(i);
		}
		RevertChecker.airRevertQueue.clear();
	}

	private void manageFirebending() {
		FireStream.progressAll();
		FireStream.removeAllNoneFireIgnitedBlock();
		
		
		FireBall.progressAll();
		WallOfFire.progressAll();
		Lightning.progressAll();
		FireShield.progressAll();
		FireProtection.progressAll();
		FireBlast.progressAll();
		FireBurst.progressAll();
		FireJet.progressAll();
		FireStream.dissipateAll();
		Cook.progressAll();
		Illumination.progressAll();
		Enflamed.handleFlames();
		FireBlade.progressAll();
		Combustion.progressAll();
	}

	private void manageChiBlocking() {
		RapidPunch.progressAll();
		C4.progressAll();
	}

	private void manageWaterbending() {
		FreezeMelt.progressAll();		
		WaterSpout.progressAll();
		WaterManipulation.progressAll();
		WaterWall.progressAll();
		Wave.progressAll();
		IceSpike.progressAll();
		IceSpike2.progressAll();
		Torrent.progressAll();
		TorrentBurst.progressAll();
		Bloodbending.progressAll();
		HealingWaters.progressAll();
		FastSwimming.progressAll();
		OctopusForm.progressAll();
		
		Plantbending.regrow();
		WaterReturn.progressAll();
	}

	private void handleDayNight() {
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
	}

}
