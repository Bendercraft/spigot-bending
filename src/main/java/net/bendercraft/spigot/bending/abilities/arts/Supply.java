package net.bendercraft.spigot.bending.abilities.arts;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.EntityTools;

@ABendingAbility(name = Supply.NAME, affinity = BendingAffinity.BOW)
public class Supply extends BendingActiveAbility {
	public final static String NAME = "Supply";
	
	public final static String LORE_BOW = "Bow";
	public final static String LORE_ARROW = "Bow";
	
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
		ItemMeta metaBow = bow.getItemMeta();
		metaBow.setLore(Arrays.asList(LORE_BOW));
		bow.setItemMeta(metaBow);
		EntityTools.giveItemInMainHand(player, bow);
		
		ItemStack arrow = new ItemStack(Material.ARROW, 1);
		ItemMeta metaArrow = bow.getItemMeta();
		metaArrow.setLore(Arrays.asList(LORE_ARROW));
		arrow.setItemMeta(metaArrow);
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
				&& item.getItemMeta() != null
				&& item.getItemMeta().hasLore()
				&& item.getItemMeta().getLore().contains(LORE_BOW);
	}

	public static boolean isArrow(ItemStack item) {
		return item != null
				&& item.getType() == Material.ARROW
				&& item.getItemMeta() != null
				&& item.getItemMeta().hasLore()
				&& item.getItemMeta().getLore().contains(LORE_ARROW);
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void stop() {
		
	}

}
