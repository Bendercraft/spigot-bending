package net.avatarrealms.minecraft.bending.abilities.fire;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.utils.EntityTools;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Cook implements IAbility {

	private static Map<Player, Cook> instances = new HashMap<Player, Cook>();

	private static final long cooktime = 2000;
	private static final Material[] cookables = {Material.RAW_BEEF,
			Material.RAW_CHICKEN, 
			Material.RAW_FISH,
			Material.PORK,
			Material.POTATO_ITEM,
			Material.POISONOUS_POTATO};

	private Player player;
	private ItemStack items;
	private long time;
	private IAbility parent;

	public Cook(Player player, IAbility parent) {
		this.parent = parent;
		this.player = player;
		items = player.getItemInHand();
		time = System.currentTimeMillis();
		if (isCookable(items.getType())) {
			instances.put(player, this);
		}
	}

	private void progress() {
		if (player.isDead() || !player.isOnline()) {
			cancel();
			return;
		}

		if (!player.isSneaking()
				|| EntityTools.getBendingAbility(player) != Abilities.HeatControl) {
			cancel();
			return;
		}

		if (!items.equals(player.getItemInHand())) {
			time = System.currentTimeMillis();
			items = player.getItemInHand();
		}

		if (!isCookable(items.getType())) {
			cancel();
			return;
		}

		if (System.currentTimeMillis() > time + cooktime) {
			cook();
			time = System.currentTimeMillis();
		}

		player.getWorld().playEffect(player.getEyeLocation(),
				Effect.MOBSPAWNER_FLAMES, 0, 10);
	}

	private void cancel() {
		instances.remove(player);
	}

	private static boolean isCookable(Material material) {
		return Arrays.asList(cookables).contains(material);
	}

	private void cook() {
		Material cooked = getCooked(items.getType());
		ItemStack newitem = new ItemStack(cooked);
		HashMap<Integer, ItemStack> cantfit = player.getInventory().addItem(
				newitem);
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
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer != null) {
			bPlayer.earnXP(BendingType.Fire, this);
		}
	}

	private Material getCooked(Material material) {
		Material cooked = Material.AIR;
		switch (material) {
			case RAW_BEEF:
				cooked = Material.COOKED_BEEF;
				break;
			case RAW_FISH:
				cooked = Material.COOKED_FISH;
				break;
			case RAW_CHICKEN:
				cooked = Material.COOKED_CHICKEN;
				break;
			case PORK:
				cooked = Material.GRILLED_PORK;
				break;
			case POTATO_ITEM:
			case POISONOUS_POTATO : 
				cooked = Material.BAKED_POTATO;
				break;
			default:
				break;
		}
		return cooked;
	}

	public static void progressAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).progress();
		}
	}

	public static void removeAll() {
		instances.clear();
	}

	@Override
	public int getBaseExperience() {
		return 0;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}
