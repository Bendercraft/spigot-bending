package net.avatar.realms.spigot.bending.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.TempPotionEffect;
import net.avatar.realms.spigot.bending.abilities.earth.CompactColumn;
import net.avatar.realms.spigot.bending.abilities.earth.EarthColumn;
import net.avatar.realms.spigot.bending.abilities.earth.EarthTunnel;
import net.avatar.realms.spigot.bending.abilities.earth.MetalBending;
import net.avatar.realms.spigot.bending.abilities.fire.Enflamed;
import net.avatar.realms.spigot.bending.abilities.fire.FireStream;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.Tools;

public class BendingManager implements Runnable {
	public Bending plugin;
	private long time;
	private List<World> worlds = new LinkedList<World>();
	private Map<World, Boolean> nights = new HashMap<World, Boolean>();
	private Map<World, Boolean> days = new HashMap<World, Boolean>();

	public BendingManager(Bending bending) {
		this.plugin = bending;
		this.time = System.currentTimeMillis();
	}

	@Override
	public void run() {
		try {
			Bending.time_step = System.currentTimeMillis() - this.time;
			this.time = System.currentTimeMillis();

			AbilityManager.getManager().progressAllAbilities();

			manageEarthbending();
			manageFirebending();
			TempPotionEffect.progressAll();
			FlyingPlayer.handleAll();
			handleDayNight();
		}
		catch (Exception e) {
			AbilityManager.getManager().stopAllAbilities();
			PluginTools.stopAllBending();
			this.plugin.getLogger().log(Level.SEVERE, "Exception in bending loop", e);
		}

	}

	private void manageEarthbending() {
		EarthColumn.progressAll();
		CompactColumn.progressAll();
		EarthTunnel.progressAll();
		MetalBending.progressAll();

		Set<Block> copy = new HashSet<Block>(RevertChecker.revertQueue.keySet());
		for (Block block : copy) {
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
		FireStream.removeAllNoneFireIgnitedBlock();

		FireStream.dissipateAll();
		Enflamed.progressAll();
	}

	private void handleDayNight() {
		for (World world : this.plugin.getServer().getWorlds()) {
			if ((world.getWorldType() == WorldType.NORMAL)
					&& !this.worlds.contains(world)) {
				this.worlds.add(world);
				this.nights.put(world, false);
				this.days.put(world, false);
			}
		}

		List<World> removeWorlds = new LinkedList<World>();
		for (World world : this.worlds) {
			if (!this.plugin.getServer().getWorlds().contains(world)) {
				removeWorlds.add(world);
				continue;
			}
			boolean night = this.nights.get(world);
			boolean day = this.days.get(world);
			if (Tools.isDay(world) && !day) {
				for (Player player : world.getPlayers()) {
					if (EntityTools.isBender(player, BendingType.Fire)
							&& player
							.hasPermission("bending.message.daymessage")) {
						ChatColor color = ChatColor.WHITE;
						color = PluginTools.getColor(Settings.getColorString("Fire"));
						player.sendMessage(color
								+ "You feel the strength of the rising sun empowering your firebending.");
					}
				}
				this.days.put(world, true);
			}

			if (!Tools.isDay(world) && day) {
				for (Player player : world.getPlayers()) {
					if (EntityTools.isBender(player, BendingType.Fire)
							&& player
							.hasPermission("bending.message.daymessage")) {
						ChatColor color = ChatColor.WHITE;
						color = PluginTools.getColor(Settings.getColorString("Fire"));
						player.sendMessage(color
								+ "You feel the empowering of your firebending subside as the sun sets.");
					}
				}
				this.days.put(world, false);
			}

			if (Tools.isNight(world) && !night) {
				for (Player player : world.getPlayers()) {
					if (EntityTools.isBender(player, BendingType.Water)
							&& player
							.hasPermission("bending.message.nightmessage")) {
						ChatColor color = ChatColor.WHITE;
						color = PluginTools.getColor(Settings.getColorString("Water"));
						player.sendMessage(color
								+ "You feel the strength of the rising moon empowering your waterbending.");
					}
				}
				this.nights.put(world, true);
			}

			if (!Tools.isNight(world) && night) {
				for (Player player : world.getPlayers()) {
					if (EntityTools.isBender(player, BendingType.Water)
							&& player
							.hasPermission("bending.message.nightmessage")) {
						ChatColor color = ChatColor.WHITE;
						color = PluginTools.getColor(Settings.getColorString("Water"));
						player.sendMessage(color
								+ "You feel the empowering of your waterbending subside as the moon sets.");
					}
				}
				this.nights.put(world, false);
			}
		}

		for (World world : removeWorlds) {
			this.worlds.remove(world);
		}
	}

}
