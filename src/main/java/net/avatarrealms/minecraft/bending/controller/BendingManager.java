package net.avatarrealms.minecraft.bending.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.abilities.air.AirBlast;
import net.avatarrealms.minecraft.bending.abilities.air.AirBubble;
import net.avatarrealms.minecraft.bending.abilities.air.AirBurst;
import net.avatarrealms.minecraft.bending.abilities.air.AirPassive;
import net.avatarrealms.minecraft.bending.abilities.air.AirScooter;
import net.avatarrealms.minecraft.bending.abilities.air.AirShield;
import net.avatarrealms.minecraft.bending.abilities.air.AirSpout;
import net.avatarrealms.minecraft.bending.abilities.air.AirSuction;
import net.avatarrealms.minecraft.bending.abilities.air.AirSwipe;
import net.avatarrealms.minecraft.bending.abilities.air.Speed;
import net.avatarrealms.minecraft.bending.abilities.air.Tornado;
import net.avatarrealms.minecraft.bending.abilities.chi.RapidPunch;
import net.avatarrealms.minecraft.bending.abilities.earth.Catapult;
import net.avatarrealms.minecraft.bending.abilities.earth.CompactColumn;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthArmor;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthBlast;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthColumn;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthPassive;
import net.avatarrealms.minecraft.bending.abilities.earth.EarthTunnel;
import net.avatarrealms.minecraft.bending.abilities.earth.Shockwave;
import net.avatarrealms.minecraft.bending.abilities.earth.Tremorsense;
import net.avatarrealms.minecraft.bending.abilities.fire.Cook;
import net.avatarrealms.minecraft.bending.abilities.fire.Enflamed;
import net.avatarrealms.minecraft.bending.abilities.fire.FireBlast;
import net.avatarrealms.minecraft.bending.abilities.fire.FireBurst;
import net.avatarrealms.minecraft.bending.abilities.fire.FireJet;
import net.avatarrealms.minecraft.bending.abilities.fire.FireShield;
import net.avatarrealms.minecraft.bending.abilities.fire.FireStream;
import net.avatarrealms.minecraft.bending.abilities.fire.Fireball;
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
import net.avatarrealms.minecraft.bending.abilities.water.WaterPassive;
import net.avatarrealms.minecraft.bending.abilities.water.WaterReturn;
import net.avatarrealms.minecraft.bending.abilities.water.WaterSpout;
import net.avatarrealms.minecraft.bending.abilities.water.WaterWall;
import net.avatarrealms.minecraft.bending.abilities.water.Wave;
import net.avatarrealms.minecraft.bending.model.AvatarState;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.TempPotionEffect;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BendingManager implements Runnable {
	public static ArrayList<Player> flyingplayers = new ArrayList<Player>();

	public Bending plugin;
	private long time;
	private long interval;
	private List<World> worlds = new ArrayList<World>();
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
			interval = System.currentTimeMillis() - time;
			time = System.currentTimeMillis();
			Bending.time_step = interval;

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

		} catch (Exception e) {
			PluginTools.stopAllBending();
			PluginTools.writeToLog("Bending broke!");
			PluginTools.writeToLog(ExceptionUtils.getStackTrace(e));
			PluginTools.verbose("Bending just broke! It seems to have saved itself. The cause was reported in bending.log, and is repeated here for your convenience:");
			e.printStackTrace();
		}

	}

	private void manageAirbending() {
		AirPassive.handlePassive(plugin.getServer());
		AirBubble.handleBubbles(plugin.getServer());

		AirBlast.progressAll();
		AirShield.progressAll();
		AirSuction.progressAll();
		AirSwipe.progressAll();
		Speed.progressAll();
		Tornado.progressAll();
		AirBurst.progressAll();
		AirScooter.progressAll();
		AirSpout.spoutAll();
	}

	private void manageEarthbending() {

		for (int ID : Catapult.instances.keySet()) {
			Catapult.progress(ID);
		}

		for (int ID : EarthColumn.instances.keySet()) {
			EarthColumn.progress(ID);
		}

		for (int ID : CompactColumn.instances.keySet()) {
			CompactColumn.progress(ID);
		}

		for (int ID : EarthBlast.instances.keySet()) {
			EarthBlast.progress(ID);
		}

		for (Player player : EarthTunnel.instances.keySet()) {
			EarthTunnel.progress(player);
		}

		for (Player player : EarthArmor.instances.keySet()) {
			EarthArmor.moveArmor(player);
		}
		EarthPassive.revertSands();

		Shockwave.progressAll();

		Tremorsense.manage(plugin.getServer());

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

		// for (Block block : RevertChecker.movedEarthQueue.keySet()) {
		// block.setType(RevertChecker.movedEarthQueue.get(block));
		// RevertChecker.movedEarthQueue.remove(block);
		// }

	}

	private void manageFirebending() {

		for (int ID : FireStream.instances.keySet()) {
			FireStream.progress(ID);
		}

		for (Block block : FireStream.ignitedblocks.keySet()) {
			if (block.getType() != Material.FIRE) {
				FireStream.ignitedblocks.remove(block);
			}
		}

		Fireball.progressAll();

		WallOfFire.manage();

		Lightning.progressAll();

		FireShield.progressAll();

		FireBlast.progressAll();

		FireBurst.progressAll();

		FireJet.progressAll();

		FireStream.dissipateAll();

		Cook.progressAll();

		Illumination.manage(plugin.getServer());

		Enflamed.handleFlames();

	}

	private void manageChiBlocking() {
		for (Player p : RapidPunch.instance.keySet())
			RapidPunch.instance.get(p).startPunch(p);
	}

	private void manageWaterbending() {

		FreezeMelt.handleFrozenBlocks();

		WaterSpout.handleSpouts(plugin.getServer());

		for (int ID : WaterManipulation.instances.keySet()) {
			WaterManipulation.progress(ID);
		}

		for (int ID : WaterWall.instances.keySet()) {
			WaterWall.progress(ID);
		}

		for (int ID : Wave.instances.keySet()) {
			Wave.progress(ID);
		}

		for (int ID : IceSpike.instances.keySet()) {
			IceSpike.instances.get(ID).progress();
		}

		for (int ID : IceSpike.instances.keySet()) {
			IceSpike.instances.get(ID).progress();
		}

		IceSpike2.progressAll();

		Torrent.progressAll();
		TorrentBurst.progressAll();

		Bloodbending.progressAll();

		HealingWaters.heal(plugin.getServer());

		WaterPassive.handlePassive(plugin.getServer());
		FastSwimming.HandleSwim(plugin.getServer());
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
