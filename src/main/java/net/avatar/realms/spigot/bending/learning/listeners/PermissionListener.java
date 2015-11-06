package net.avatar.realms.spigot.bending.learning.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.learning.BendingLearning;

public class PermissionListener implements Listener {
	private BendingLearning plugin;

	public PermissionListener(BendingLearning plugin) {
		this.plugin = plugin;

	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		plugin.lease(event.getPlayer());

		for(BendingAbilities ab : BendingAbilities.values()) {
			// Add default perm to allow base ability to be used
			if(plugin.isBasicBendingAbility(ab)) {
				plugin.addPermission(event.getPlayer(), ab);
			}
			
			// Also allow all abilities if it is link to an affinity and player has it
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(event.getPlayer());
			if(ab.getAffinity() != null && bPlayer != null && bPlayer.hasAffinity(ab.getAffinity())) {
				plugin.addPermission(event.getPlayer(), ab);
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		this.plugin.release(event.getPlayer());
	}
}
