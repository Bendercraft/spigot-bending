package net.avatar.realms.spigot.bending.listeners;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import net.avatar.realms.spigot.bending.Bending;

public class BendingDenyItem implements Listener {
	
	private Map<Enchantment, Integer> deniedEnchantments;
	private List<PotionType> deniedPotions;
	
	public BendingDenyItem() {
		deniedEnchantments = new HashMap<Enchantment, Integer>();
		deniedPotions = new LinkedList<PotionType>();
		
		deniedEnchantments.put(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
		deniedEnchantments.put(Enchantment.PROTECTION_FIRE, 0);
		deniedEnchantments.put(Enchantment.PROTECTION_EXPLOSIONS, 4);
		deniedEnchantments.put(Enchantment.PROTECTION_PROJECTILE, 4);
		deniedEnchantments.put(Enchantment.OXYGEN, 3);
		deniedEnchantments.put(Enchantment.WATER_WORKER, 1);
		deniedEnchantments.put(Enchantment.THORNS, 1);
		deniedEnchantments.put(Enchantment.DAMAGE_ALL, 2);
		deniedEnchantments.put(Enchantment.DAMAGE_UNDEAD, 5);
		deniedEnchantments.put(Enchantment.DAMAGE_ARTHROPODS, 5);
		deniedEnchantments.put(Enchantment.KNOCKBACK, 2);
		deniedEnchantments.put(Enchantment.FIRE_ASPECT, 0);
		deniedEnchantments.put(Enchantment.LOOT_BONUS_MOBS, 3);
		deniedEnchantments.put(Enchantment.DIG_SPEED, 5);
		deniedEnchantments.put(Enchantment.SILK_TOUCH, 1);
		deniedEnchantments.put(Enchantment.DURABILITY, 3);
		deniedEnchantments.put(Enchantment.LOOT_BONUS_BLOCKS, 3);
		deniedEnchantments.put(Enchantment.ARROW_DAMAGE, 3);
		deniedEnchantments.put(Enchantment.ARROW_KNOCKBACK, 2);
		deniedEnchantments.put(Enchantment.ARROW_FIRE, 0);
		deniedEnchantments.put(Enchantment.ARROW_INFINITE, 1);
		deniedEnchantments.put(Enchantment.LUCK, 3);
		deniedEnchantments.put(Enchantment.LURE, 3);
		
		deniedPotions.add(PotionType.STRENGTH);
		deniedPotions.add(PotionType.FIRE_RESISTANCE);
	}
	
	@EventHandler
    public void onEnchantItemEvent(EnchantItemEvent event) {  
		sanitize(event.getItem());
	}
	
	@EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
		if(event.getClickedInventory().getHolder() instanceof Player) {
			sanitize((Player) event.getClickedInventory().getHolder());
		}
	}
	
	@EventHandler
    public void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
		sanitize(event.getItem().getItemStack());
	}
	
	@EventHandler
    public void onPlayerItemHeldEvent(PlayerItemHeldEvent event) {
		sanitize(event.getPlayer().getInventory().getItem(event.getNewSlot()));
	}
	
	@EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		if(event.getDamager() instanceof Player) {
			Player p = (Player) event.getDamager();
			sanitize(p.getInventory().getItemInMainHand());
			sanitize(p.getInventory().getItemInOffHand());
		}
	}
	
	@EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
		sanitize(event.getItem());
	}
	
	
	@EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
		sanitize(event.getPlayer());
	}
	
	private void sanitize(Player player) {
		sanitize(player.getItemOnCursor());
		sanitize(player.getInventory().getItemInMainHand());
		sanitize(player.getInventory().getItemInOffHand());
		for(ItemStack itemstack : player.getInventory().getContents()) {
			sanitize(itemstack);
		}
		for(ItemStack itemstack : player.getInventory().getArmorContents()) {
			sanitize(itemstack);
		}
		for(ItemStack itemstack : player.getEnderChest()) {
			sanitize(itemstack);
		}
	}
	
	private void sanitize(ItemStack item) {
		if(item == null) {
			return;
		}
		for(Entry<Enchantment, Integer> entry : deniedEnchantments.entrySet()) {
			Enchantment enchantment = entry.getKey();
			int level = entry.getValue();
			
			if(!item.containsEnchantment(enchantment )) {
				continue;
			}
			if(level == 0) {
				item.removeEnchantment(enchantment);
				continue;
			}
			if(item.getEnchantmentLevel(enchantment) >= level) {
				item.removeEnchantment(enchantment);
				item.addEnchantment(enchantment, level);
			}
		}
		
		if(item.getType() == Material.POTION) {
			Potion potion = Potion.fromItemStack(item);
			if(deniedPotions.contains(potion.getType())) {
				potion.setType(PotionType.WATER);
				ItemStack corrected = potion.toItemStack(1);
				item.setAmount(corrected.getAmount());
				item.setData(corrected.getData());
				item.setDurability(corrected.getDurability());
				item.setItemMeta(corrected.getItemMeta());
				item.setType(corrected.getType());
			}
		}
		if(item.getType() == Material.SHIELD) {
			item.setType(Material.WOOD);
			item.setAmount(item.getAmount());
			try {
				item.setData(Material.WOOD.getData().newInstance());
			} catch (InstantiationException e) {
				Bending.getInstance().getLogger().log(Level.SEVERE, "Cannot instanciate WOOD default MaterialData", e);
			} catch (IllegalAccessException e) {
				Bending.getInstance().getLogger().log(Level.SEVERE, "Cannot instanciate WOOD default MaterialData", e);
			}
			item.setDurability(Material.WOOD.getMaxDurability());
			item.setItemMeta(null);
			
		}
	}
}
