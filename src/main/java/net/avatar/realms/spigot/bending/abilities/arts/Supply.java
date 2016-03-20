package net.avatar.realms.spigot.bending.abilities.arts;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;

@ABendingAbility(name = Supply.NAME, affinity = BendingAffinity.BOW)
public class Supply extends BendingActiveAbility {
	public final static String NAME = "Supply";
	
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
		ItemMeta metaBow = bow.getItemMeta();
		metaBow.setDisplayName("§3Master Bow");
		metaBow.setLore(Arrays.asList("Bow"));
		bow.setItemMeta(metaBow);
		EntityTools.giveItemInMainHand(player, bow);
		
		ItemStack arrow = new ItemStack(Material.ARROW, 1);
		ItemMeta metaArrow = bow.getItemMeta();
		metaArrow.setLore(Arrays.asList("Arrow"));
		arrow.setItemMeta(metaArrow);
		player.getInventory().addItem(arrow);
		
		bender.cooldown(this, COOLDOWN);
		return false;
	}

	@Override
	public void progress() {
		
	}
	
	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void stop() {
		
	}

}
