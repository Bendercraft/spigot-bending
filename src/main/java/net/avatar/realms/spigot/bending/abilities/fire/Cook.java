package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@BendingAbility(name="Heat Control", element=BendingType.Fire)
public class Cook implements IAbility {

	private static Map<Player, Cook> instances = new HashMap<Player, Cook>();


	@ConfigurationParameter("Cook-Time")
	private static long COOK_TIME = 2000;
	private static final Material[] cookables = {Material.RAW_BEEF,
			Material.RAW_CHICKEN, 
			Material.RAW_FISH,
			Material.PORK,
			Material.POTATO_ITEM,
			Material.POISONOUS_POTATO,
			Material.STICK};

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

	private boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			return false;
		}

		if (!player.isSneaking()
				|| EntityTools.getBendingAbility(player) != Abilities.HeatControl) {
			return false;
		}

		if (!items.equals(player.getItemInHand())) {
			time = System.currentTimeMillis();
			items = player.getItemInHand();
		}

		if (!isCookable(items.getType())) {
			return false;
		}

		if (System.currentTimeMillis() > time + COOK_TIME) {
			cook();
			time = System.currentTimeMillis();
		}

		player.getWorld().playEffect(player.getEyeLocation(),
				Effect.MOBSPAWNER_FLAMES, 0, 10);
		return true;
	}

	private void remove() {
		instances.remove(player);
	}

	private static boolean isCookable(Material material) {
		return Arrays.asList(cookables).contains(material);
	}

	private void cook() {
		Material cooked = getCooked(items.getType());
		ItemStack newitem = new ItemStack(cooked);
		if (cooked == Material.TORCH) {
			newitem.setAmount(4);
		}
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
			case STICK:
				cooked = Material.TORCH;
				break;
			default:
				break;
		}
		return cooked;
	}

	public static void progressAll() {
		List<Cook> toRemove = new LinkedList<Cook>();
		for (Cook cook : instances.values()) {
			boolean keep = cook.progress();
			if(!keep) {
				toRemove.add(cook);
			}
		}
		for (Cook cook : toRemove) {
			cook.remove();
		}
	}

	public static void removeAll() {
		instances.clear();
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}
