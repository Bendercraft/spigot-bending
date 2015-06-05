package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.Tools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class MetalWire {

	private static Map<Player, Fish> instances = new HashMap<Player, Fish>();
	private static Map<Player, Long> noFall = new HashMap<Player, Long>();
	
	private final static long timeNoFall = 2200;

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

				Vector dir = Tools.getVectorForPoints(playerLoc, targetLoc);
				player.setVelocity(dir);
				noFall.put(player, System.currentTimeMillis());
				int slot = player.getInventory().getHeldItemSlot();
				ItemStack rod = player.getInventory().getItem(slot);
				if (rod != null && rod.getType()== Material.FISHING_ROD) {
					rod.setDurability((short) (rod.getDurability() - 2));
					player.getInventory().setItem(slot, rod);
				}
			}

			instances.remove(player);
		} else {
			// if the list doesn't contain the player, it means he just launched
			// the hook
			launchHook(player, hook);
		}
	}

	public static void launchHook(Player player, Fish hook) {
		//Would prefer it more accurate
		instances.put(player, hook);
		Block b = player.getTargetBlock(new HashSet<Material>(), 30);
		if (b != null) {
			hook.setVelocity(Tools.getVectorForPoints(hook.getLocation(),
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
