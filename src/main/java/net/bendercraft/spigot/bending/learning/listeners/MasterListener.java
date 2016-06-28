package net.bendercraft.spigot.bending.learning.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.arts.Dash;
import net.bendercraft.spigot.bending.controller.Settings;
import net.bendercraft.spigot.bending.learning.BendingLearning;
import net.bendercraft.spigot.bending.utils.PluginTools;

public class MasterListener implements Listener {
	private BendingLearning plugin;

	private Map<UUID, Double> sprintDistanceTraveledDash = new HashMap<UUID, Double>();
	private Map<UUID, Location> sprintLastLocationDash = new HashMap<UUID, Location>();
	private static final int distanceNeededDash = 1000;

	private static ChatColor color = PluginTools.getColor(Settings.getColor(BendingElement.MASTER));

	public MasterListener(BendingLearning plugin) {
		this.plugin = plugin;

	}

	@EventHandler
	public void unlockDash(PlayerToggleSprintEvent event) {
		Player pl = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(pl);
		if (bPlayer != null) {
			if (bPlayer.isBender(BendingElement.MASTER)) {
				if (!pl.isSprinting()) {
					sprintLastLocationDash.put(pl.getUniqueId(), pl.getLocation().clone());
				} else {
					if (sprintLastLocationDash.containsKey(pl.getUniqueId())) {
						Location last = sprintLastLocationDash.get(pl.getUniqueId());
						Location current = pl.getLocation();
						if (last.getWorld().getUID().equals(current.getWorld().getUID())) {
							double distance = last.distance(current);
							if (sprintDistanceTraveledDash.containsKey(pl.getUniqueId())) {
								distance = sprintDistanceTraveledDash.get(pl.getUniqueId()) + distance;
							}
							if (distance >= distanceNeededDash) {
								if (plugin.addPermission(pl, Dash.NAME)) {

									String message = "Wooo !";
									pl.sendMessage(color + message);
									message = "Congratulations, you have unlocked " + Dash.NAME;
									pl.sendMessage(color + message);
								}
								sprintDistanceTraveledDash.remove(pl.getUniqueId());
							} else {
								sprintDistanceTraveledDash.put(pl.getUniqueId(), distance);
							}
						}
					}
				}
			}
		}
	}
}
