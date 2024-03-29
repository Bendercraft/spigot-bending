package net.bendercraft.spigot.bending.abilities.earth;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import net.bendercraft.spigot.bending.abilities.*;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.EntityTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;

@ABendingAbility(name = MetalBending.NAME, affinity = BendingAffinity.METAL)
public class MetalBending extends BendingActiveAbility {
	public final static String NAME = "Metalbending";
	
	@ConfigurationParameter("Melt-Time")
	private static long MELT_TIME = 2000;

	private static Map<Material, Integer> metals = new HashMap<>();
	static {
		metals.put(Material.IRON_SHOVEL, 1);
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
		metals.put(Material.RAIL, 6);
		metals.put(Material.TRIPWIRE, 1);
		metals.put(Material.IRON_TRAPDOOR, 2);
	}

	private long time;
	private ItemStack items;

	public MetalBending(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean sneak() {
		if (getState() != BendingAbilityState.START) {
			return false;
		}

		this.time = System.currentTimeMillis();
		this.items = player.getInventory().getItemInMainHand();
		if (isMeltable(this.items.getType())) {
			setState(BendingAbilityState.PROGRESSING);
		}
		return false;
	}

	public static void use(Player player, Block block) {
		// Don't really like it, magic value
		if (EntityTools.isBender(player, BendingElement.EARTH)
				&& NAME.equals(EntityTools.getBendingAbility(player))) {
			if (EntityTools.canBend(player, NAME)) {
				if (block.getType() == Material.IRON_DOOR) {
					RegisteredAbility registered = AbilityManager.getManager().getRegisteredAbility(NAME);
					if (!ProtectionManager.isLocationProtectedFromBending(player, registered, block.getLocation())) {
						Door data = (Door) block.getBlockData();

						if (data.isOpen()) {
							data.setOpen(false);
							block.getWorld().playSound(block.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1.0f, 1.0f);
						}
						else {
							data.setOpen(true);
							block.getWorld().playSound(block.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1.0f, 1.0f);
						}

						block.setBlockData(data);
					}
				}
			}
		}
	}

	public static boolean isMeltable(Material m) {
		return metals.containsKey(m);
	}
	
	@Override
	public boolean canTick() {
		if(!super.canTick()) {
			return false;
		}
		if (!player.isSneaking() || !NAME.equals(EntityTools.getBendingAbility(player))) {
			return false;
		}
		return true;
	}

	public void progress() {
		if (!items.equals(player.getInventory().getItemInMainHand())) {
			time = System.currentTimeMillis();
			items = player.getInventory().getItemInMainHand();
		}

		if (!isMeltable(items.getType())) {
			remove();
			return;
		}

		if (System.currentTimeMillis() > time + MELT_TIME) {
			melt();
			time = System.currentTimeMillis();
		}
	}

	private void melt() {
		ItemStack newItem = new ItemStack(Material.IRON_INGOT);
		int max = items.getType().getMaxDurability();
		int nb = metals.get(items.getType());
		if (max > 0) {
			int cur = max;
			if(items.hasItemMeta() && items.getItemMeta() instanceof Damageable) {
				Damageable meta = (Damageable) items.getItemMeta();
				cur -= meta.getDamage();
			}
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
		HashMap<Integer, ItemStack> cantfit = player.getInventory().addItem(newItem);
		for (int id : cantfit.keySet()) {
			player.getWorld().dropItem(player.getEyeLocation(), cantfit.get(id));
		}
		int amount = items.getAmount();
		if (amount == 1) {
			player.getInventory().clear(player.getInventory().getHeldItemSlot());
		} else {
			items.setAmount(amount - 1);
		}
		player.sendMessage("melted");
	}

	@Override
	public Object getIdentifier() {
		return player;
	}

	@Override
	public void stop() {
		
	}

}
