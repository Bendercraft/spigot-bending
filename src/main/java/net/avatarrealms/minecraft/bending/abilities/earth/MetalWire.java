package net.avatarrealms.minecraft.bending.abilities.earth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.utils.BlockTools;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class MetalWire {

	private static Map<Player, Fish> players = new HashMap<Player, Fish>();

	// Will have to replace Fish by FishHook when available

	public static void pull(Player player, Fish hook) {
		List<Player> toRemove = new LinkedList<Player>();
		for (Player p : players.keySet()) {
			if (players.get(p).isDead() || !players.get(p).isValid()) {
				Bukkit.getLogger().info("Dead");
				toRemove.add(p);
			}
		}

		for (Player p : toRemove) {
			players.remove(p);
		}

		if (players.containsKey(player)) {
			Bukkit.getLogger().info("Ground");
			Location loc = hook.getLocation().add(0, -1, 0);
			if (!BlockTools.isFluid(loc.getBlock())) {
				Bukkit.getLogger().info("Not Fluid");
				Location targetLoc = loc.clone().add(0, 1.5, 0);
				Location playerLoc = player.getLocation();

				Vector dir = getVectorForPoints(playerLoc, targetLoc);
				player.setVelocity(dir);
			}

			players.remove(player);
		} else {
			Bukkit.getLogger().info("Launch");
			// if the list doesn't contain the player, it means he just launched
			// the hook
			launchHook(player, hook);
		}
	}

	public static Vector getVectorForPoints(Location l1, Location l2) {
		double g = -0.08;
		double d = l2.distance(l1);
		double t = d;
		double vX = (1.0 + 0.07 * t) * (l2.getX() - l1.getX()) / t;
		double vY = (1.0 + 0.03 * t) * (l2.getY() - l1.getY()) / t - 0.5 * g
				* t;
		double vZ = (1.0 + 0.07 * t) * (l2.getZ() - l1.getZ()) / t;
		return new Vector(vX, vY, vZ);
	}

	public static void launchHook(Player player, Fish hook) {
		players.put(player, hook);
		Block b = player.getTargetBlock(null, 30);
		if (b != null) {
			hook.setVelocity(getVectorForPoints(hook.getLocation(),
					b.getLocation()));
		}
	}
}
