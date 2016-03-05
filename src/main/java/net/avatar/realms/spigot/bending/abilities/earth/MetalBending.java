package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.HashMap;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@ABendingAbility(name = MetalBending.NAME, affinity = BendingAffinity.METAL)
public class MetalBending extends BendingActiveAbility {
	public final static String NAME = "Metalbending";
	
	@ConfigurationParameter("Melt-Time")
	private static long MELT_TIME = 2000;

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

	@SuppressWarnings("deprecation")
	public static void use(Player pl, Block bl) {
		// Don't really like it, magic value
		if (EntityTools.isBender(pl, BendingElement.EARTH) 
				&& NAME.equals(EntityTools.getBendingAbility(pl))) {
			if (EntityTools.canBend(pl, NAME)) {
				if (bl.getType() == Material.IRON_DOOR_BLOCK) {
					if (bl.getData() >= 8) {
						bl = bl.getRelative(BlockFace.DOWN);
					}
					if (bl.getType() == Material.IRON_DOOR_BLOCK) {
						if (!ProtectionManager.isLocationProtectedFromBending(pl, NAME, bl.getLocation())) {
							if (bl.getData() < 4) {
								bl.setData((byte) (bl.getData() + 4));
								bl.getWorld().playEffect(bl.getLocation(), Effect.DOOR_TOGGLE, 0);
							} else {
								bl.setData((byte) (bl.getData() - 4));
								bl.getWorld().playEffect(bl.getLocation(), Effect.DOOR_TOGGLE, 0);
							}
						}
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
		if (!player.isSneaking() || !EntityTools.getBendingAbility(player).equals(NAME)) {
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
