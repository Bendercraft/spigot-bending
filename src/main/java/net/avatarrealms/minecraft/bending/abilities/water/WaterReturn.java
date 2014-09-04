package net.avatarrealms.minecraft.bending.abilities.water;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.model.TempBlock;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

public class WaterReturn implements IAbility {

	private static Map<Player, WaterReturn> instances = new HashMap<Player, WaterReturn>();
	// private static int ID = Integer.MIN_VALUE;
	private static long interval = 50;

	private static final byte full = 0x0;
	private static double range = 30;

	private Player player;
	// private int id;
	private Location location;
	private TempBlock block;
	private long time;
	private IAbility parent;

	public WaterReturn(Player player, Block block, IAbility parent) {
		this.parent = parent;
		if (instances.containsKey(player))
			return;
		this.player = player;
		location = block.getLocation();
		if (!Tools.isRegionProtectedFromBuild(player,
				Abilities.WaterManipulation, location)
				&& EntityTools.canBend(player, Abilities.WaterManipulation)) {
			if (BlockTools.isTransparentToEarthbending(player, block)
					&& !block.isLiquid()) {
				this.block = new TempBlock(block, Material.WATER, full);
			}
		}

		instances.put(player, this);
	}

	private boolean progress() {
		if (!hasEmptyWaterBottle()) {
			return false;
		}

		if (player.isDead() || !player.isOnline()) {
			return false;
		}

		if (player.getWorld() != location.getWorld()) {
			return false;
		}

		if (System.currentTimeMillis() < time + interval)
			return true;

		time = System.currentTimeMillis();

		Vector direction = Tools
				.getDirection(location, player.getEyeLocation()).normalize();
		location = location.clone().add(direction);

		if (location == null || block == null) {
			return false;
		}

		if (location.getBlock().equals(block.getLocation().getBlock()))
			return true;

		if (Tools.isRegionProtectedFromBuild(player,
				Abilities.WaterManipulation, location)) {
			return false;
		}

		if (location.distance(player.getEyeLocation()) > PluginTools
				.waterbendingNightAugment(range, player.getWorld())) {
			return false;
		}

		if (location.distance(player.getEyeLocation()) <= 1.5) {
			fillBottle();
			return false;
		}

		Block newblock = location.getBlock();
		if (BlockTools.isTransparentToEarthbending(player, newblock)
				&& !newblock.isLiquid()) {
			block.revertBlock();
			block = new TempBlock(newblock, Material.WATER, full);
		} else {
			return false;
		}
		return true;
	}
	
	private void clear() {
		if (block != null) {
			block.revertBlock();
			block = null;
		}
	}

	private void remove() {
		this.clear();
		instances.remove(player);
	}

	private boolean hasEmptyWaterBottle() {
		PlayerInventory inventory = player.getInventory();
		if (inventory.contains(Material.GLASS_BOTTLE)) {
			return true;
		}
		return false;
	}

	private void fillBottle() {
		PlayerInventory inventory = player.getInventory();
		if (inventory.contains(Material.GLASS_BOTTLE)) {
			int index = inventory.first(Material.GLASS_BOTTLE);
			ItemStack item = inventory.getItem(index);
			if (item.getAmount() == 1) {
				inventory.setItem(index, new ItemStack(Material.POTION));
			} else {
				item.setAmount(item.getAmount() - 1);
				inventory.setItem(index, item);
				Map<Integer, ItemStack> leftover = inventory
						.addItem(new ItemStack(Material.POTION));
				for (int left : leftover.keySet()) {
					player.getWorld().dropItemNaturally(player.getLocation(),
							leftover.get(left));
				}
			}
		}
	}

	private static boolean isBending(Player player) {
		if(WaterManipulation.isWaterManipulater(player)) {
			return true;
		}

		if (OctopusForm.isOctopus(player))
			return true;

		if (Wave.isWaving(player))
			return true;

		if(WaterWall.isWaterWalling(player)) {
			return true;
		}

		if (IceSpike2.isBending(player))
			return true;

		return false;
	}

	public static boolean hasWaterBottle(Player player) {
		if (instances.containsKey(player))
			return false;
		if (isBending(player))
			return false;
		PlayerInventory inventory = player.getInventory();
		return (inventory.contains(new ItemStack(Material.POTION), 1));
	}

	public static void emptyWaterBottle(Player player) {
		PlayerInventory inventory = player.getInventory();
		int index = inventory.first(new ItemStack(Material.POTION));
		if (index != -1) {
			ItemStack item = inventory.getItem(index);
			if (item.getAmount() == 1) {
				inventory.setItem(index, new ItemStack(Material.GLASS_BOTTLE));
			} else {
				item.setAmount(item.getAmount() - 1);
				inventory.setItem(index, item);
				HashMap<Integer, ItemStack> leftover = inventory
						.addItem(new ItemStack(Material.GLASS_BOTTLE));
				for (int left : leftover.keySet()) {
					player.getWorld().dropItemNaturally(player.getLocation(),
							leftover.get(left));
				}
			}
		}
	}

	public static void progressAll() {
		List<WaterReturn> toRemove = new LinkedList<WaterReturn>();
		for (WaterReturn water : instances.values()) {
			boolean keep = water.progress();
			if(!keep) {
				toRemove.add(water);
			}
		}
		for(WaterReturn water : toRemove) {
			water.remove();
		}
	}

	public static void removeAll() {
		for (WaterReturn water : instances.values()) {
			if (water.block != null)
				water.block.revertBlock();
		}
		instances.clear();
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}
