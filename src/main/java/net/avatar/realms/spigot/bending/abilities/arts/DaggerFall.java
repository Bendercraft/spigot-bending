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

@ABendingAbility(name = DaggerFall.NAME, affinity = BendingAffinity.SWORD)
public class DaggerFall extends BendingActiveAbility {
	public final static String NAME = "DaggerFall";
	
	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 15000;
	
	@ConfigurationParameter("Range")
	private static int RANGE = 3;

	public DaggerFall(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean swing() {
		
		return false;
	}

	@Override
	public boolean sneak() {
		ItemStack dagger = new ItemStack(Material.IRON_SWORD, 1);
		dagger.addEnchantment(Enchantment.KNOCKBACK, 1);
		ItemMeta meta = dagger.getItemMeta();
		meta.setLore(Arrays.asList("Dagger"));
		dagger.setItemMeta(meta);
		
		EntityTools.giveItemInHand(player, dagger);
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
