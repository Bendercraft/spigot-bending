package net.avatar.realms.spigot.bending.controller;

import java.util.*;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.fire.Enflamed;
import net.avatar.realms.spigot.bending.abilities.fire.FireStream;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.TempBlock;
import net.avatar.realms.spigot.bending.utils.Tools;

public class BendingManager implements Runnable {
	private Bending plugin;
	private long time;
	private List<World> worlds = new LinkedList<World>();
	private Map<World, Boolean> nights = new HashMap<World, Boolean>();
	private Map<World, Boolean> days = new HashMap<World, Boolean>();
	private long timestep = 1; // in ms
	
	private List<Queue> revertQueue = new LinkedList<Queue>();

	public BendingManager(Bending bending) {
		this.plugin = bending;
		this.time = System.currentTimeMillis();
	}

	@Override
	public void run() {
		try {
			long now = System.currentTimeMillis();
			timestep = now - this.time;
			this.time = now;

			AbilityManager.getManager().progressAllAbilities();
			FlyingPlayer.handleAll();
			
			manageFirebending();

			manageGlobalTempBlock();
			handleDayNight();
		} catch (Exception e) {
			try {
				AbilityManager.getManager().stopAllAbilities();
				PluginTools.stopAllBending();
				plugin.getLogger().log(Level.SEVERE, "Exception in bending loop", e);
			} catch (Exception e1) {
				plugin.getLogger().log(Level.SEVERE, "Exception in exception bending loop : this is really bad", e1);
			}
		}

	}

	private void manageGlobalTempBlock() {
		long now = System.currentTimeMillis();
		List<Queue> toRemove = new LinkedList<Queue>();
		for(Queue queue : revertQueue) {
			if(queue.started+queue.life < now) {
				for(TempBlock block : queue.blocks) {
					block.revertBlock();
				}
				toRemove.add(queue);
			}
		}
		revertQueue.removeAll(toRemove);
	}

	private void manageFirebending() {
		FireStream.removeAllNoneFireIgnitedBlock();

		FireStream.dissipateAll();
		Enflamed.progressAll();
	}

	private void handleDayNight() {
		for (World world : this.plugin.getServer().getWorlds()) {
			if ((world.getWorldType() == WorldType.NORMAL) && !this.worlds.contains(world)) {
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
					if (EntityTools.isBender(player, BendingElement.Fire) && player.hasPermission("bending.message.daymessage")) {
						ChatColor color = ChatColor.WHITE;
						color = PluginTools.getColor(Settings.getColorString("Fire"));
						player.sendMessage(color + "You feel the strength of the rising sun empowering your firebending.");
					}
				}
				this.days.put(world, true);
			}

			if (!Tools.isDay(world) && day) {
				for (Player player : world.getPlayers()) {
					if (EntityTools.isBender(player, BendingElement.Fire) && player.hasPermission("bending.message.daymessage")) {
						ChatColor color = ChatColor.WHITE;
						color = PluginTools.getColor(Settings.getColorString("Fire"));
						player.sendMessage(color + "You feel the empowering of your firebending subside as the sun sets.");
					}
				}
				this.days.put(world, false);
			}

			if (Tools.isNight(world) && !night) {
				for (Player player : world.getPlayers()) {
					if (EntityTools.isBender(player, BendingElement.Water) && player.hasPermission("bending.message.nightmessage")) {
						ChatColor color = ChatColor.WHITE;
						color = PluginTools.getColor(Settings.getColorString("Water"));
						player.sendMessage(color + "You feel the strength of the rising moon empowering your waterbending.");
					}
				}
				this.nights.put(world, true);
			}

			if (!Tools.isNight(world) && night) {
				for (Player player : world.getPlayers()) {
					if (EntityTools.isBender(player, BendingElement.Water) && player.hasPermission("bending.message.nightmessage")) {
						ChatColor color = ChatColor.WHITE;
						color = PluginTools.getColor(Settings.getColorString("Water"));
						player.sendMessage(color + "You feel the empowering of your waterbending subside as the moon sets.");
					}
				}
				this.nights.put(world, false);
			}
		}

		for (World world : removeWorlds) {
			this.worlds.remove(world);
		}
	}
	
	public void addGlobalTempBlock(long life, TempBlock... blocks) {
		List<TempBlock> temp = new LinkedList<TempBlock>();
		Collections.addAll(temp, blocks);
		addGlobalTempBlock(life, temp);
	}
	
	public void addGlobalTempBlock(long life, List<TempBlock> blocks) {
		Queue queue = new Queue();
		queue.started = System.currentTimeMillis();
		queue.life = life;
		queue.blocks = blocks;
		
		revertQueue.add(queue);
	}

	public long getTimestep() {
		return timestep;
	}

	private class Queue {
		private long started;
		private long life;
		private List<TempBlock> blocks;
	}
}
