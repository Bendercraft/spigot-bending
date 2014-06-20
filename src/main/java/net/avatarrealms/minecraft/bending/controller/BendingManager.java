package net.avatarrealms.minecraft.bending.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;

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
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.AvatarState;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.TempPotionEffect;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BendingManager implements Runnable {

	public Bending plugin;

	public static ArrayList<Player> flyingplayers = new ArrayList<Player>();

	// private static boolean safeRevert = ConfigManager.safeRevert;

	private boolean verbose = false;
	private long verbosetime;
	private long verboseinterval = 3 * 60 * 1000;

	long time;
	long interval;
	long reverttime;
	ArrayList<World> worlds = new ArrayList<World>();
	ConcurrentHashMap<World, Boolean> nights = new ConcurrentHashMap<World, Boolean>();
	ConcurrentHashMap<World, Boolean> days = new ConcurrentHashMap<World, Boolean>();

	public static final String defaultsunrisemessage = "You feel the strength of the rising sun empowering your firebending.";
	public static final String defaultsunsetmessage = "You feel the empowering of your firebending subside as the sun sets.";
	public static final String defaultmoonrisemessage = "You feel the strength of the rising moon empowering your waterbending.";
	public static final String defaultmoonsetmessage = "You feel the empowering of your waterbending subside as the moon sets.";
	
	public BendingManager(Bending bending) {
		plugin = bending;
		time = System.currentTimeMillis();
		verbosetime = System.currentTimeMillis();
		reverttime = time;
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
			
			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
				bPlayer.increaseAllBendingCpt();
			}

			if (verbose
					&& System.currentTimeMillis() > verbosetime
							+ verboseinterval)
				handleVerbosity();

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

		for (int ID : AirShield.instances.keySet()) {
			AirShield.progress(ID);
		}

		AirSuction.progressAll();

		for (int ID : AirSwipe.instances.keySet()) {
			AirSwipe.progress(ID);
		}

		for (int ID : Speed.instances.keySet()) {
			Speed.progress(ID);
		}

		for (int ID : Tornado.instances.keySet()) {
			Tornado.progress(ID);
		}

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

		ArrayList<World> removeworlds = new ArrayList<World>();
		for (World world : worlds) {
			if (!plugin.getServer().getWorlds().contains(world)) {
				removeworlds.add(world);
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
				days.replace(world, true);
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
				days.replace(world, false);
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
				nights.replace(world, true);
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
				nights.replace(world, false);
			}
		}

		for (World world : removeworlds) {
			worlds.remove(world);
		}

	}

	// private void manageMessages() {
	// for (Player player : newplayers) {
	// player.sendMessage(ChatColor.GOLD
	// + "Use '/bending choose <element>' to get started!");
	// }
	// newplayers.clear();
	// }

	private void handleVerbosity() {
		verbosetime = System.currentTimeMillis();

		int airblasts, airbubbles, airscooters, airshields, airsuctions, airswipes, tornados; // ,airbursts,
		// airspouts;

		int airblastplayers = 0;
		airblasts = AirBlast.instances.size();

		int airbubbleplayers = 0;
		airbubbles = AirBubble.instances.size();

		// int airburstplayers = 0;
		// airbursts = AirBurst.instances.size();

		int airscooterplayers = 0;
		airscooters = AirScooter.instances.size();

		int airshieldplayers = 0;
		airshields = AirShield.instances.size();

		// int airspoutplayer = 0;
		// airspouts = AirSpout.instances.size();

		int airsuctionplayers = 0;
		airsuctions = AirSuction.instances.size();

		int airswipeplayers = 0;
		airswipes = AirSwipe.instances.size();

		int tornadoplayers = 0;
		tornados = Tornado.instances.size();

		int catapults, compactcolumns, earthblasts, earthcolumns, earthtunnels, tremorsenses; // ,shockwaves;

		int catapultplayers = 0;
		catapults = Catapult.instances.size();

		int compactcolumnplayers = 0;
		compactcolumns = CompactColumn.instances.size();

		int earthblastplayers = 0;
		earthblasts = EarthBlast.instances.size();

		int earthcolumnplayers = 0;
		earthcolumns = EarthColumn.instances.size();

		int earthtunnelplayers = 0;
		earthtunnels = EarthTunnel.instances.size();

		// int shockwaveplayers = 0;
		// shockwaves = Shockwave.instances.size();

		int tremorsenseplayers = 0;
		tremorsenses = Tremorsense.instances.size();

		int fireballs, fireblasts, firejets, firestreams, illuminations, walloffires; // ,lightings;

		int fireblastplayers = 0;
		fireblasts = FireBlast.instances.size();

		int firestreamplayers = 0;
		firestreams = FireStream.instances.size();

		int fireballplayers = 0;
		fireballs = Fireball.instances.size();

		int firejetplayers = 0;
		firejets = FireJet.instances.size();

		int illuminationplayers = 0;
		illuminations = Illumination.instances.size();

		// int lightningplayers = 0;
		// lightnings = Lightning.instances.size();

		int walloffireplayers = 0;
		walloffires = WallOfFire.instances.size();

		int bloodbendings, freezemelts, watermanipulations, waterspouts, waterwalls, waves;

		int bloodbendingplayers = 0;
		bloodbendings = Bloodbending.instances.size();

		int freezemeltplayers = 0;
		freezemelts = FreezeMelt.frozenblocks.size();

		int watermanipulationplayers = 0;
		watermanipulations = WaterManipulation.instances.size();

		int waterspoutplayers = 0;
		waterspouts = WaterSpout.instances.size();

		int waterwallplayers = 0;
		waterwalls = WaterWall.instances.size();

		int waveplayers = 0;
		waves = Wave.instances.size();

		for (Player player : plugin.getServer().getOnlinePlayers()) {
			Abilities ability = EntityTools.getBendingAbility(player);
			if (ability == Abilities.AirBlast)
				airblastplayers++;
			if (ability == Abilities.AirBubble)
				airbubbleplayers++;
			if (ability == Abilities.AirScooter)
				airscooterplayers++;
			if (ability == Abilities.AirShield)
				airshieldplayers++;
			if (ability == Abilities.AirSuction)
				airsuctionplayers++;
			if (ability == Abilities.AirSwipe)
				airswipeplayers++;
			if (ability == Abilities.Tornado)
				tornadoplayers++;
			if (ability == Abilities.Catapult)
				catapultplayers++;
			if (ability == Abilities.Collapse)
				compactcolumnplayers++;
			if (ability == Abilities.EarthBlast)
				earthblastplayers++;
			if (ability == Abilities.RaiseEarth)
				earthcolumnplayers++;
			if (ability == Abilities.EarthGrab)
				earthcolumnplayers++;
			if (ability == Abilities.EarthTunnel)
				earthtunnelplayers++;
			if (EntityTools.hasAbility(player, Abilities.Tremorsense))
				tremorsenseplayers++;
			if (ability == Abilities.Blaze)
				firestreamplayers++;
			if (ability == Abilities.FireBlast)
				fireballplayers++;
			if (ability == Abilities.FireBlast)
				fireblastplayers++;
			if (EntityTools.hasAbility(player, Abilities.FireJet))
				firejetplayers++;
			if (EntityTools.hasAbility(player, Abilities.Illumination))
				illuminationplayers++;
			if (ability == Abilities.WallOfFire)
				walloffireplayers++;
			if (ability == Abilities.Bloodbending)
				bloodbendingplayers++;
			if (EntityTools.hasAbility(player, Abilities.PhaseChange))
				freezemeltplayers++;
			if (ability == Abilities.WaterBubble)
				airbubbleplayers++;
			if (ability == Abilities.WaterManipulation)
				watermanipulationplayers++;
			if (EntityTools.hasAbility(player, Abilities.WaterSpout))
				waterspoutplayers++;
			if (ability == Abilities.Surge)
				waterwallplayers++;
			if (ability == Abilities.Surge)
				waveplayers++;
		}

		PluginTools.writeToLog("Debug data at "
				+ Calendar.getInstance().get(Calendar.HOUR) + "h "
				+ Calendar.getInstance().get(Calendar.MINUTE) + "m "
				+ Calendar.getInstance().get(Calendar.SECOND) + "s");

		verbose("airblasts", airblasts, airblastplayers, false);
		verbose("airbubbles", airbubbles, airbubbleplayers, true);
		// verbose("airbursts", airbursts, airburstplayers, false);
		verbose("airscooters", airscooters, airscooterplayers, true);
		verbose("airshields", airshields, airshieldplayers, true);
		// verbose("airspouts", airspouts, airspoutplayers, false);
		verbose("airsuctions", airsuctions, airsuctionplayers, false);
		verbose("airswipes", airswipes, airswipeplayers, false);
		verbose("tornados", tornados, tornadoplayers, true);

		verbose("catapults", catapults, catapultplayers, true);
		verbose("compactcolumns", compactcolumns, compactcolumnplayers, false);
		verbose("earthblasts", earthblasts, earthblastplayers, true);
		verbose("earthcolumns", earthcolumns, earthcolumnplayers, false);
		verbose("earthtunnels", earthtunnels, earthtunnelplayers, true);
		// verbose("shockwaves", shockwaves, shockwaveplayers, false);
		verbose("tremorsenses", tremorsenses, tremorsenseplayers, true);

		verbose("fireballs", fireballs, fireballplayers, false);
		verbose("fireblasts", fireblasts, fireblastplayers, false);
		verbose("firejets", firejets, firejetplayers, true);
		verbose("firestreams", firestreams, firestreamplayers, false);
		verbose("illuminations", illuminations, illuminationplayers, true);
		// verbose("lightnings", lightnings, lightningplayers, true);
		verbose("walloffires", walloffires, walloffireplayers, false);

		verbose("bloodbendings", bloodbendings, bloodbendingplayers, true);
		verbose("freezemelts", freezemelts, freezemeltplayers, false);
		verbose("watermanipulations", watermanipulations,
				watermanipulationplayers, false);
		verbose("waterspouts", waterspouts, waterspoutplayers, true);
		verbose("waterwalls", waterwalls, waterwallplayers, true);
		verbose("waves", waves, waveplayers, true);

		PluginTools.writeToLog(null);
	}

	private void verbose(String name, int instances, int players,
			boolean warning) {
		if (warning && instances > players) {
			name = "==WARNING== " + name;
		}
		PluginTools.writeToLog(name + ": " + instances + " instances for " + players
				+ " players.");
	}

}
