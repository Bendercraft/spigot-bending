package net.bendercraft.spigot.bending.abilities.earth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import com.google.common.collect.Sets;

import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.Tools;

public class MetalWire {

	private static Map<Player, FishHook> instances = new HashMap<Player, FishHook>();
	private static Map<Player, Long> noFall = new HashMap<Player, Long>();

	@ConfigurationParameter("Time-No-Fall")
	private static long TIME_NO_FALL = 2200;

	public static void pull(Player player, FishHook hook) {
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
				if (rod != null && rod.getType() == Material.FISHING_ROD) {
					ItemMeta meta = rod.getItemMeta();
					Damageable metaDamageable = (Damageable) meta;
					metaDamageable.setDamage(metaDamageable.getDamage() - 2);
					rod.setItemMeta(meta);
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

	public static void launchHook(Player player, FishHook hook) {
		// Would prefer it more accurate
		instances.put(player, hook);
		Block b = player.getTargetBlock(BlockTools.getAirs(), 30);
		if (b != null) {
			hook.setVelocity(Tools.getVectorForPoints(hook.getLocation(), b.getLocation()));
		}
	}

	public static boolean hookHangsOn(FishHook hook) {
		for (Block block : BlockTools.getBlocksAroundPoint(hook.getLocation(), 1.5)) {
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
			if (System.currentTimeMillis() > noFall.get(p) + TIME_NO_FALL) {
				toRemove.add(p);
			} else {
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
