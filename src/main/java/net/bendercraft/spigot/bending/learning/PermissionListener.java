package net.bendercraft.spigot.bending.learning;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;

public class PermissionListener implements Listener {
	private BendingLearning plugin;

	public PermissionListener(BendingLearning plugin) {
		this.plugin = plugin;

	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		plugin.lease(event.getPlayer());

		for(RegisteredAbility ab : AbilityManager.getManager().getRegisteredAbilities()) {
			// Add default perm to allow base ability to be used
			if(plugin.isBasicBendingAbility(ab.getName())) {
				plugin.addPermission(event.getPlayer(), ab.getName());
			}
			
			// Also allow all abilities if it is link to an affinity and player has it
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(event.getPlayer());
			if(ab.getAffinity() != null && bPlayer != null && bPlayer.hasAffinity(ab.getAffinity())) {
				plugin.addPermission(event.getPlayer(), ab.getName());
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		this.plugin.release(event.getPlayer());
	}
}
