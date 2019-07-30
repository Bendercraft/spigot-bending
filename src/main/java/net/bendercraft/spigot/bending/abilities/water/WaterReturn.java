package net.bendercraft.spigot.bending.abilities.water;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.BendingAbility;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;
import net.bendercraft.spigot.bending.utils.Tools;

public class WaterReturn {
	private static long interval = 50;
	private static double range = 30;

	private Location location;
	private TempBlock block;
	private long time;
	private Player player;
	private final RegisteredAbility waterRegister;
	private BendingAbility parent;

	public WaterReturn(Player player, Block block, BendingAbility parent) {
		location = block.getLocation();
		this.player = player;
		this.parent = parent;
		this.waterRegister = parent.getRegister();
		if (!ProtectionManager.isLocationProtectedFromBending(player, waterRegister, location) && EntityTools.canBend(player, waterRegister)) {
			if (BlockTools.isTransparentToEarthbending(player, block) && !block.isLiquid()) {
				this.block = TempBlock.makeTemporary(parent, block, Material.WATER, false);
			}
		}
	}

	public boolean progress() {
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

		Vector direction = Tools.getDirection(location, player.getEyeLocation()).normalize();
		location = location.clone().add(direction);

		if (location == null || block == null) {
			return false;
		}

		if (location.getBlock().equals(block.getLocation().getBlock()))
			return true;

		if (ProtectionManager.isLocationProtectedFromBending(player, waterRegister, location)) {
			return false;
		}

		if (location.distance(player.getEyeLocation()) > range) {
			return false;
		}

		if (location.distance(player.getEyeLocation()) <= 1.5) {
			fillBottle();
			return false;
		}

		Block newblock = location.getBlock();
		if (BlockTools.isTransparentToEarthbending(player, newblock) && !newblock.isLiquid()) {
			block.revertBlock();
			block = TempBlock.makeTemporary(parent, newblock, Material.WATER, false);
		} else {
			return false;
		}
		return true;
	}

	public void stop() {
		if (block != null) {
			block.revertBlock();
			block = null;
		}
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

			ItemStack filledBottle = new ItemStack(Material.POTION);
			PotionMeta filledBottleMeta = (PotionMeta) filledBottle.getItemMeta();
			filledBottleMeta.setBasePotionData(new PotionData(PotionType.WATER));
			filledBottle.setItemMeta(filledBottleMeta);

			if (item.getAmount() == 1) {
				inventory.setItem(index, filledBottle);
			} else {
				item.setAmount(item.getAmount() - 1);
				inventory.setItem(index, item);
				Map<Integer, ItemStack> leftover = inventory.addItem(filledBottle);
				for (int left : leftover.keySet()) {
					player.getWorld().dropItemNaturally(player.getLocation(), leftover.get(left));
				}
			}
		}
	}

	private static boolean isBending(Player player) {
		if (WaterManipulation.isWaterManipulater(player)) {
			return true;
		}

		if (Wave.isWaving(player))
			return true;

		if (WaterWall.isWaterWalling(player)) {
			return true;
		}

		return false;
	}

	public static boolean hasWaterBottle(Player player) {
		if (isBending(player)) {
			return false;
		}
		return getWaterBottle(player.getInventory()) != -1;
	}
	
	public static boolean canBeSource(Block block) {
		if (TempBlock.isTempBlock(block)) {
			return false;
		}
		return BlockTools.isAir(block);
	}
	
	private static int getWaterBottle(Inventory inventory) {
		for(int i = 0 ; i < inventory.getContents().length ; i++) {
			ItemStack is = inventory.getContents()[i];
			if(is == null || is.getType() != Material.POTION) {
				continue;
			}
			PotionMeta pm = (PotionMeta) is.getItemMeta();
			if(pm.getBasePotionData().getType() == PotionType.WATER) {
				return i;
			}
		}
		return -1;
	}

	public static void emptyWaterBottle(Player player) {
		PlayerInventory inventory = player.getInventory();
		int index = getWaterBottle(inventory);
		if (index != -1) {
			ItemStack item = inventory.getItem(index);
			if (item.getAmount() == 1) {
				inventory.setItem(index, new ItemStack(Material.GLASS_BOTTLE));
			} else {
				item.setAmount(item.getAmount() - 1);
				inventory.setItem(index, item);
				HashMap<Integer, ItemStack> leftover = inventory.addItem(new ItemStack(Material.GLASS_BOTTLE));
				for (int left : leftover.keySet()) {
					player.getWorld().dropItemNaturally(player.getLocation(), leftover.get(left));
				}
			}
		}
	}

}
