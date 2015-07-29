package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.controller.ConfigManager;
import net.avatar.realms.spigot.bending.utils.EntityTools;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
//import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@BendingAbility(name="Fire Blade", element=BendingType.Fire)
public class FireBlade {
	private static Map<Player, FireBlade> instances = new HashMap<Player, FireBlade>();
	private static String LORE_NAME = "FireBlade";
	private static final Enchantment sharp = Enchantment.DAMAGE_ALL;
	private static int sharpnessLevel = ConfigManager.fireBladeSharpnessLevel;
	private static final Enchantment dura = Enchantment.DURABILITY;
	private static final int duraLevel = 3;
	//private static int strengthLevel = ConfigManager.fireBladeStrengthLevel;
	private static int duration = ConfigManager.fireBladeDuration;
	private static final Enchantment knockback = Enchantment.KNOCKBACK;
	
	private ItemStack blade;
	//private static PotionEffect strengthEffect;
	
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
		List<FireBlade> toRemove = new LinkedList<FireBlade>();
		for (FireBlade blade : instances.values()) {
			boolean keep = blade.progress();
			if (!keep) {
				toRemove.add(blade);
			}
		}
		
		for (FireBlade pl : toRemove) {
			pl.remove();
		}
		
	}
	
	public boolean progress() {
		if (player.getPlayer() == null 
				|| player.getPlayer().isDead() 
				|| !player.getPlayer().isOnline()) {
			return false;
		}
		
		if(blade == null) {
			return false;
		}
		
		if (System.currentTimeMillis() > time + (1000*duration)) {
			return false;
		}
		
		if(!isFireBlade(player.getItemInHand())) {
			return false;
		}
		
		if (EntityTools.getBendingAbility(player) != Abilities.FireBlade) {
			return false;
		}
		return true;
	}
	
	public void remove() {
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
		instances.remove(player);
	}
	
	public static void removeFireBlade(ItemStack is) {
		FireBlade toRemove = null;
		for (FireBlade blade : instances.values()) {
			if(blade.getBlade() != null && isFireBlade(blade.getBlade())) {
				toRemove = blade;
			}
		}
		
		if(toRemove != null) {
			toRemove.remove();
		}
	}
	
	public ItemStack getBlade() {
		return blade;
	}
	
	public static boolean isFireBlade(ItemStack is) {
		if(is == null) {
			return false;
		}
		if(is.getItemMeta() != null 
				&& is.getItemMeta().getLore() != null
				&& is.getItemMeta().getLore().contains(LORE_NAME)) {
			return true;
		}
		return false;
	}
	
	public static boolean isFireBlading(Player p)  {
		return instances.containsKey(p);
	}
	
	public static FireBlade getFireBlading(Player p) {
		return instances.get(p);
	}
	
	public void giveFireBlade() {
		ItemStack fireB = new ItemStack(Material.GOLD_SWORD);
		if (sharpnessLevel > 0) {
			fireB.addEnchantment(sharp, sharpnessLevel);
		}
		if (duraLevel > 0) {
			fireB.addEnchantment(dura, duraLevel);
		}
		fireB.addEnchantment(knockback, 1);
		List<String> lore = new LinkedList<String>();
		lore.add(LORE_NAME);
		ItemMeta meta = fireB.getItemMeta();
		meta.setLore(lore);
		fireB.setItemMeta(meta);
		
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
		blade = fireB;
	}
	
	public static void removeAll() {
		List<FireBlade> toRemove = new LinkedList<FireBlade>(instances.values());
		for (FireBlade blade : toRemove) {
			blade.remove();
		}
		instances.clear();
	}

}
