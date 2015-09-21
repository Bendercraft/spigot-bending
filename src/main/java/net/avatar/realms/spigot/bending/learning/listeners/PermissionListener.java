package net.avatar.realms.spigot.bending.learning.listeners;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.learning.BendingLearning;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PermissionListener implements Listener {
	private BendingLearning plugin;

	public PermissionListener(BendingLearning plugin) {
		this.plugin = plugin;

	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		this.plugin.lease(event.getPlayer());

		// Add default perm to allow base ability to be used
		this.plugin.addPermission(event.getPlayer(), BendingAbilities.AirBlast);
		this.plugin.addPermission(event.getPlayer(), BendingAbilities.AirSpout);
		this.plugin.addPermission(event.getPlayer(), BendingAbilities.AirSwipe);

		this.plugin.addPermission(event.getPlayer(), BendingAbilities.WaterManipulation);
		this.plugin.addPermission(event.getPlayer(), BendingAbilities.WaterSpout);
		this.plugin.addPermission(event.getPlayer(), BendingAbilities.HealingWaters);

		this.plugin.addPermission(event.getPlayer(), BendingAbilities.FireBlast);
		this.plugin.addPermission(event.getPlayer(), BendingAbilities.Blaze);
		this.plugin.addPermission(event.getPlayer(), BendingAbilities.HeatControl);

		this.plugin.addPermission(event.getPlayer(), BendingAbilities.EarthBlast);
		this.plugin.addPermission(event.getPlayer(), BendingAbilities.RaiseEarth);
		this.plugin.addPermission(event.getPlayer(), BendingAbilities.Collapse);

		this.plugin.addPermission(event.getPlayer(), BendingAbilities.RapidPunch);
		this.plugin.addPermission(event.getPlayer(), BendingAbilities.VitalPoint);
		this.plugin.addPermission(event.getPlayer(), BendingAbilities.HighJump);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		this.plugin.release(event.getPlayer());
	}
}
