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
		ItemMeta metaDagger = dagger.getItemMeta();
		metaDagger.setLore(Arrays.asList("Dagger"));
		dagger.setItemMeta(metaDagger);
		
		ItemStack shield = new ItemStack(Material.SHIELD, 1);
		ItemMeta metaShield = shield.getItemMeta();
		metaShield.setLore(Arrays.asList("Shield"));
		shield.setItemMeta(metaShield);
		
		EntityTools.giveItemInMainHand(player, dagger);
		EntityTools.giveItemInOffHand(player, shield);
		
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
