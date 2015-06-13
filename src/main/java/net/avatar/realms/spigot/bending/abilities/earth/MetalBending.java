package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MetalBending {

	private static final long meltTime = 2000;
	private static Map<Player, MetalBending> instances = new HashMap<Player, MetalBending>();

	private static Map<Material, Integer> metals = new HashMap<Material, Integer>();
	static {
		metals.put(Material.IRON_SPADE, 1);
		metals.put(Material.IRON_ORE, 1);
		metals.put(Material.IRON_BLOCK, 9);
		metals.put(Material.IRON_DOOR, 6);
		metals.put(Material.IRON_AXE, 3);
		metals.put(Material.IRON_PICKAXE, 3);
		metals.put(Material.IRON_HOE, 2);
		metals.put(Material.IRON_SWORD, 2);
		metals.put(Material.IRON_HELMET, 5);
		metals.put(Material.IRON_LEGGINGS, 7);
		metals.put(Material.IRON_BOOTS, 4);
		metals.put(Material.IRON_CHESTPLATE, 8);
		metals.put(Material.SHEARS, 2);
		metals.put(Material.ANVIL, 31);
		metals.put(Material.HOPPER, 5);
		metals.put(Material.CAULDRON, 7);
		metals.put(Material.RAILS, 6);
		metals.put(Material.TRIPWIRE, 1);
		metals.put(Material.IRON_PLATE, 2);
	}

	private long time;
	private Player player;
	private ItemStack items;

	private MetalBending(Player player, ItemStack i) {
		this.player = player;
		this.time = System.currentTimeMillis();
		this.items = i;
		instances.put(player, this);
	}

	@SuppressWarnings("deprecation")
	public static void use(Player pl, Block bl) {
		// Don't really like it, magic value
		if (EntityTools.isBender(pl, BendingType.Earth)
				&& EntityTools.getBendingAbility(pl) == Abilities.MetalBending) {
			if (EntityTools.canBend(pl, Abilities.MetalBending)) {
				if (bl.getType() == Material.IRON_DOOR_BLOCK) {
					if (bl.getData() >= 8) {
						bl = bl.getRelative(BlockFace.DOWN);
					}
					if (bl.getType() == Material.IRON_DOOR_BLOCK) {
						if (!ProtectionManager.isRegionProtectedFromBending(pl,
								Abilities.MetalBending, bl.getLocation())) {
							if (bl.getData() < 4) {
								bl.setData((byte) (bl.getData() + 4));
								bl.getWorld().playEffect(bl.getLocation(),
										Effect.DOOR_TOGGLE, 0);
							} else {
								bl.setData((byte) (bl.getData() - 4));
								bl.getWorld().playEffect(bl.getLocation(),
										Effect.DOOR_TOGGLE, 0);
							}
						}
					}
				}
			}
		}
	}

	public static void metalMelt(Player player) {
		ItemStack is = player.getItemInHand();
		if (isMeltable(is.getType())) {
			new MetalBending(player, is);
		}
	}

	public static boolean isMeltable(Material m) {
		return metals.containsKey(m);
	}

	public static void progressAll() {
		List<Player> toRemove = new LinkedList<Player>();
		for (Player p : instances.keySet()) {
			boolean keep = instances.get(p).progress();
			if (!keep) {
				toRemove.add(p);
			}
		}

		for (Player p : toRemove) {
			instances.remove(p);
		}

	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			return false;
		}
		if (!player.isSneaking()
				|| EntityTools.getBendingAbility(player) != Abilities.MetalBending) {
			return false;
		}

		if (!items.equals(player.getItemInHand())) {
			time = System.currentTimeMillis();
			items = player.getItemInHand();
		}

		if (!isMeltable(items.getType())) {
			return false;
		}

		if (System.currentTimeMillis() > time + meltTime) {
			melt();
			time = System.currentTimeMillis();
		}

		return true;
	}

	private void melt() {
		ItemStack newItem = new ItemStack(Material.IRON_INGOT);
		int max = items.getType().getMaxDurability();
		int nb = metals.get(items.getType());
		if (max > 0) {
			int cur = max - items.getDurability();
			player.sendMessage(cur + "/" + max);
			double prc = (double) cur / max;

			player.sendMessage(nb + " " + prc);
			nb *= prc;
			if (nb < 1) {
				nb = 1;
			}
		}

		player.sendMessage("" + nb);
		newItem.setAmount(nb);
		HashMap<Integer, ItemStack> cantfit = player.getInventory().addItem(
				newItem);
		for (int id : cantfit.keySet()) {
			player.getWorld()
					.dropItem(player.getEyeLocation(), cantfit.get(id));
		}
		int amount = items.getAmount();
		if (amount == 1) {
			player.getInventory()
					.clear(player.getInventory().getHeldItemSlot());
		} else {
			items.setAmount(amount - 1);
		}
		player.sendMessage("melted");
	}

	public static void removeAll() {
		instances.clear();
	}

}
