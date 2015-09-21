package net.avatar.realms.spigot.bending.learning.listeners;

import net.avatar.realms.spigot.bending.abilities.Abilities;
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
		
		//Add default perm to allow base ability to be used
		this.plugin.addPermission(event.getPlayer(), Abilities.AirBlast);
		this.plugin.addPermission(event.getPlayer(), Abilities.AirSpout);
		this.plugin.addPermission(event.getPlayer(), Abilities.AirSwipe);
		
		this.plugin.addPermission(event.getPlayer(), Abilities.WaterManipulation);
		this.plugin.addPermission(event.getPlayer(), Abilities.WaterSpout);
		this.plugin.addPermission(event.getPlayer(), Abilities.HealingWaters);
		
		this.plugin.addPermission(event.getPlayer(), Abilities.FireBlast);
		this.plugin.addPermission(event.getPlayer(), Abilities.Blaze);
		this.plugin.addPermission(event.getPlayer(), Abilities.HeatControl);
		
		this.plugin.addPermission(event.getPlayer(), Abilities.EarthBlast);
		this.plugin.addPermission(event.getPlayer(), Abilities.RaiseEarth);
		this.plugin.addPermission(event.getPlayer(), Abilities.Collapse);
		
		this.plugin.addPermission(event.getPlayer(), Abilities.RapidPunch);
		this.plugin.addPermission(event.getPlayer(), Abilities.VitalPoint);
		this.plugin.addPermission(event.getPlayer(), Abilities.HighJump);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		this.plugin.release(event.getPlayer());
	}
}
