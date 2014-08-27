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
		if (players.contains(player)) {	
			Bukkit.getLogger().info("Bounce");
			Location loc = hook.getLocation().add(0,-1,0);
			if (!BlockTools.isFluid(loc.getBlock())) {
				Bukkit.getLogger().info("Not Fluid");
				Location targetLoc = loc.clone().add(0, 1.5, 0);
				Location playerLoc = player.getLocation();
				
				Vector dir = getVectorForPoints(playerLoc, targetLoc);
				player.setVelocity(dir);	
			}
			players.remove(player);
		} 
		else {
			Bukkit.getLogger().info("Doesn't bounce");
			//if the list doesn't contain the player, it means he just launched the hook
			players.add(player);
		}
	}
	
	public static Vector getVectorForPoints (Location l1, Location l2) {
		double g = -0.08;
		double d = l2.distance(l1);
		double t = d;
		double vX = (1.0 + 0.07*t)*(l2.getX() - l1.getX())/t;
		double vY = (1.0 + 0.03*t)*(l2.getY() - l1.getY())/t - 0.5*g*t;
		double vZ = (1.0 + 0.07*t)*(l2.getZ() - l1.getZ())/t;
		return new Vector(vX, vY, vZ);
	}
}
