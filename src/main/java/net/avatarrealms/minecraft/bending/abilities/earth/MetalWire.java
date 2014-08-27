package net.avatarrealms.minecraft.bending.abilities.earth;

import java.util.LinkedList;
import java.util.List;

import net.avatarrealms.minecraft.bending.utils.BlockTools;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class MetalWire {
	private static List<Player> players = new LinkedList<Player>();
	
	public static void pull(Player player, Fish hook) {
		Bukkit.getLogger().info("Entered the pull method");
		if (players.contains(player)) {
			Bukkit.getLogger().info("Contains");
			Location loc = hook.getLocation().add(0,-1,0);
			if (!BlockTools.isFluid(loc.getBlock())) {
				Bukkit.getLogger().info("Fluid");
				Location targetLoc = loc.clone().add(0,1,0);
				Location playerLoc = player.getLocation();
				
				Vector dir = targetLoc.subtract(playerLoc).toVector();
				player.setVelocity(dir);	
			}
			players.remove(player);
		} 
		else {
			Bukkit.getLogger().info("Doesn't contain");
			//if the list doesn't contain the player, it means he just launched the hook
			players.add(player);
		}
	}
}
