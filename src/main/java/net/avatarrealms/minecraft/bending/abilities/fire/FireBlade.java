package net.avatarrealms.minecraft.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.EntityTools;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FireBlade {
	
	private static Map<Player, FireBlade> instances = new HashMap<Player, FireBlade>();
	
	private static final Enchantment sharp = Enchantment.DAMAGE_ALL;
	private static int sharpnessLevel = ConfigManager.fireBladeSharpnessLevel;
	private static final Enchantment fire = Enchantment.FIRE_ASPECT;
	private static int fireAspectLevel = ConfigManager.fireBladeFireAspectLevel;
	private static final Enchantment dura = Enchantment.DURABILITY;
	private static final int duraLevel = 3;
	//private static int strengthLevel = ConfigManager.fireBladeStrengthLevel;
	private static int duration = ConfigManager.fireBladeDuration;
	private static final Enchantment knockback = Enchantment.KNOCKBACK;
	private static PotionEffect strengthEffect;
	
	private long time;
	private Player player;
	
	public FireBlade (Player player) {	
		BendingPlayer bP = BendingPlayer.getBendingPlayer(player);
		if (bP == null) {
			return;
		}
		if (bP.isOnCooldown(Abilities.FireBlade)) {
			return;
		}
		this.player = player;
		this.time = System.currentTimeMillis();
		
		giveFireBlade();
		instances.put(player, this);
		bP.cooldown(Abilities.FireBlade);
	}
	
	
	public static void progressAll() {		
		List<Player> toRemove = new LinkedList<Player>();
		for (Player player : instances.keySet()) {
			boolean keep = instances.get(player).progress();
			if (!keep) {
				instances.get(player).removeFireBlade();
				toRemove.add(player);
			}
		}
		
		for (Player pl : toRemove) {
			instances.remove(pl);
		}
		
	}
	
	public boolean progress() {
		if (player.getPlayer().isDead() || !player.getPlayer().isOnline()) {
			return false;
		}
		
		if (System.currentTimeMillis() > time + (1000*duration)) {
			return false;
		}
		
		if (EntityTools.getBendingAbility(player) != Abilities.FireBlade) {
			return false;
		}
		return true;
	}
	
	public void removeFireBlade() {
		ItemStack toRemove = null;
		for (ItemStack is : player.getInventory().getContents()) {
			if (is != null && isFireBlade(is)) {
				toRemove = is;
				break;
			}
		}
		if (toRemove != null) {
			player.getInventory().remove(toRemove);
		}
		player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
	}
	
	public static boolean isFireBlade(ItemStack is) {
		if (is == null) {
			return false;
		}
		
		if (is.getType() != Material.GOLD_SWORD) {
			return false;
		}
		
		if (!is.containsEnchantment(sharp)) {
			return false;
		}
		
		if (!is.containsEnchantment(fire)) {
			return false;
		}
		
		if (!is.containsEnchantment(dura)) {
			return false;
		}
		
		return true;
	}
	
	public static boolean isFireBlading(Player p)  {
		return instances.containsKey(p);
	}
	
	public void giveFireBlade() {
		ItemStack fireB = new ItemStack(Material.GOLD_SWORD);
		if (sharpnessLevel > 0) {
			fireB.addEnchantment(sharp, sharpnessLevel);
		}
		
		if (fireAspectLevel > 0) {
			fireB.addEnchantment(fire, fireAspectLevel);
		}
		
		if (duraLevel > 0) {
			fireB.addEnchantment(dura, duraLevel);
		}
		
		fireB.addEnchantment(knockback, 1);
		
		
		int slot = player.getInventory().getHeldItemSlot();
		ItemStack hand = player.getInventory().getItem(slot);
		if (hand != null) {
			int i = player.getInventory().firstEmpty();
			if (i != -1) {
				//else the player will loose his hand item, his bad
				player.getInventory().setItem(i,hand);
			}
		}
		player.getInventory().setItem(slot,fireB);
		/*if (strengthLevel > 0) {
			strengthEffect = new PotionEffect(PotionEffectType.INCREASE_DAMAGE,
					duration*20, strengthLevel-1);
			player.addPotionEffect(strengthEffect);
		}*/
	}
	
	public static void removeAll() {
		for (Player p : instances.keySet()) {
			instances.get(p).removeFireBlade();
		}
		instances.clear();
	}

}
