package net.bendercraft.spigot.bending.abilities.arts;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.EntityTools;
import org.bukkit.persistence.PersistentDataType;

@ABendingAbility(name = Supply.NAME, affinity = BendingAffinity.BOW)
public class Supply extends BendingActiveAbility {
	public final static String NAME = "Supply";

	private final static NamespacedKey BOW_KEY = new NamespacedKey(Bending.getInstance(), "Supply_bow");
	private final static NamespacedKey ARROW_KEY = new NamespacedKey(Bending.getInstance(), "Supply_arrow");
	
	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 15000;
	
	@ConfigurationParameter("Range")
	private static int RANGE = 3;

	public Supply(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean swing() {
		
		return false;
	}

	@Override
	public boolean sneak() {
		ItemStack bow = new ItemStack(Material.BOW, 1);
		bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
		bow.addEnchantment(Enchantment.ARROW_KNOCKBACK, 2);
		ItemMeta bowMeta = bow.getItemMeta();
		bowMeta.getPersistentDataContainer().set(BOW_KEY, PersistentDataType.SHORT, (short)1);
		bow.setItemMeta(bowMeta);
		EntityTools.giveItemInMainHand(player, bow);
		
		ItemStack arrow = new ItemStack(Material.ARROW, 1);
		ItemMeta arrowMeta = arrow.getItemMeta();
		arrowMeta.getPersistentDataContainer().set(ARROW_KEY, PersistentDataType.SHORT, (short)1);
		arrow.setItemMeta(arrowMeta);
		player.getInventory().addItem(arrow);
		
		bender.cooldown(this, COOLDOWN);
		return false;
	}

	@Override
	public void progress() {
		
	}

	public static boolean isBow(ItemStack item) {
		return item != null
				&& item.getType() == Material.BOW
				&& item.hasItemMeta()
				&& item.getItemMeta().getPersistentDataContainer().has(BOW_KEY, PersistentDataType.SHORT);
	}

	public static boolean isArrow(ItemStack item) {
		return item != null
				&& item.getType() == Material.ARROW
				&& item.hasItemMeta()
				&& item.getItemMeta().getPersistentDataContainer().has(ARROW_KEY, PersistentDataType.SHORT);
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void stop() {
		
	}

}
