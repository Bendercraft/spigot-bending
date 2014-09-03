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

	private static Map<Player, Fish> instances = new HashMap<Player, Fish>();
	private static Map<Player, Long> noFall = new HashMap<Player, Long>();
	
	private final static long timeNoFall = 1500;

	// Will have to replace Fish by FishHook when available

	public static void pull(Player player, Fish hook) {
		List<Player> toRemove = new LinkedList<Player>();
		for (Player p : instances.keySet()) {
			if (instances.get(p).isDead() || !instances.get(p).isValid()) {
				toRemove.add(p);
			}
		}

		for (Player p : toRemove) {
			instances.remove(p);
		}

		if (instances.containsKey(player)) {
			if (hookHangsOn(hook)) {
				Location targetLoc = hook.getLocation().clone().add(0, 1.5, 0);
				Location playerLoc = player.getLocation();

				Vector dir = getVectorForPoints(playerLoc, targetLoc);
				player.setVelocity(dir);
				noFall.put(player, System.currentTimeMillis());
			}

			instances.remove(player);
		} else {
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
		//Would prefer it more accurate
		instances.put(player, hook);
		Block b = player.getTargetBlock(null, 30);
		if (b != null) {
			hook.setVelocity(getVectorForPoints(hook.getLocation(),
					b.getLocation()));
		}
	}
	
	public static boolean hookHangsOn(Fish hook) {
		for (Block block : BlockTools.getBlocksAroundPoint(hook.getLocation(),1.5)) {
			if (!BlockTools.isFluid(block)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean hasNoFallDamage(Player pl) {
		boolean r = false;
		List<Player> toRemove = new LinkedList<Player>();
		for (Player p : noFall.keySet()) {
			if (System.currentTimeMillis() > noFall.get(p) + timeNoFall) {
				toRemove.add(p);
			} 
			else {
				if (p.equals(pl)) {
					r = true;
				}
			}
		}
		
		for (Player p : toRemove) {
			noFall.remove(p);
		}
		return r;
	}
	
}
