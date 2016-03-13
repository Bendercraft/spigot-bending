package net.avatar.realms.spigot.bending.listeners;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.controller.Settings;

public class BendingDenyItem implements Listener {
	
	private Map<UUID, Long> enderpearls;
	
	private Map<Enchantment, Integer> deniedEnchantments;
	private List<PotionType> deniedPotions;
	
	public BendingDenyItem() {
		enderpearls = new HashMap<UUID, Long>();
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
		deniedEnchantments.put(Enchantment.KNOCKBACK, 1);
		deniedEnchantments.put(Enchantment.FIRE_ASPECT, 0);
		deniedEnchantments.put(Enchantment.LOOT_BONUS_MOBS, 3);
		deniedEnchantments.put(Enchantment.DIG_SPEED, 5);
		deniedEnchantments.put(Enchantment.SILK_TOUCH, 1);
		deniedEnchantments.put(Enchantment.DURABILITY, 3);
		deniedEnchantments.put(Enchantment.LOOT_BONUS_BLOCKS, 3);
		deniedEnchantments.put(Enchantment.ARROW_DAMAGE, 0);
		deniedEnchantments.put(Enchantment.ARROW_KNOCKBACK, 0);
		deniedEnchantments.put(Enchantment.ARROW_FIRE, 0);
		deniedEnchantments.put(Enchantment.ARROW_INFINITE, 0);
		deniedEnchantments.put(Enchantment.LUCK, 3);
		deniedEnchantments.put(Enchantment.LURE, 3);
		
		deniedPotions.add(PotionType.STRENGTH);
		deniedPotions.add(PotionType.FIRE_RESISTANCE);
		deniedPotions.add(PotionType.INVISIBILITY);
	}
	
	@EventHandler
    public void onEnchantItemEvent(EnchantItemEvent event) {  
		sanitize(BendingPlayer.getBendingPlayer(event.getEnchanter()), event.getItem());
	}
	
	@EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
		if(event.getClickedInventory() != null 
				&& event.getClickedInventory().getHolder() instanceof Player) {
			sanitize((Player) event.getClickedInventory().getHolder());
		}
	}
	
	@EventHandler
    public void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
		sanitize(BendingPlayer.getBendingPlayer(event.getPlayer()), event.getItem().getItemStack());
	}
	
	@EventHandler
    public void onPlayerItemHeldEvent(PlayerItemHeldEvent event) {
		sanitize(BendingPlayer.getBendingPlayer(event.getPlayer()), event.getPlayer().getInventory().getItem(event.getNewSlot()));
	}
	
	@EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		if(event.getDamager() instanceof Player) {
			Player p = (Player) event.getDamager();
			BendingPlayer bender = BendingPlayer.getBendingPlayer(p);
			sanitize(bender, p.getInventory().getItemInMainHand());
			sanitize(bender, p.getInventory().getItemInOffHand());
		}
	}
	
	@EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
		sanitize(BendingPlayer.getBendingPlayer(event.getPlayer()), event.getItem());
		
		//Ender pearl ?
		if(event.getItem() != null 
				&& event.getItem().getType() == Material.ENDER_PEARL
				&& event.getAction() != Action.LEFT_CLICK_AIR
				&& event.getAction() != Action.LEFT_CLICK_BLOCK) {
			long now = System.currentTimeMillis();
			
			if(enderpearls.containsKey(event.getPlayer().getUniqueId())) {
				if(enderpearls.get(event.getPlayer().getUniqueId()) + Settings.ENDERPEARL_COOLDOWN > now) {
					event.setCancelled(true);
				}
			}
			if(!event.isCancelled()) {
				enderpearls.put(event.getPlayer().getUniqueId(), now);
			}
		}
	}
	
	
	@EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
		sanitize(event.getPlayer());
	}
	
	private void sanitize(Player player) {
		BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
		sanitize(bender, player.getItemOnCursor());
		sanitize(bender, player.getInventory().getItemInMainHand());
		sanitize(bender, player.getInventory().getItemInOffHand());
		for(ItemStack itemstack : player.getInventory().getContents()) {
			sanitize(bender, itemstack);
		}
		for(ItemStack itemstack : player.getInventory().getArmorContents()) {
			sanitize(bender, itemstack);
		}
		for(ItemStack itemstack : player.getEnderChest()) {
			sanitize(bender, itemstack);
		}
	}
	
	private void sanitize(BendingPlayer bender, ItemStack item) {
		if(item == null) {
			return;
		}
		for(Entry<Enchantment, Integer> entry : deniedEnchantments.entrySet()) {
			Enchantment enchantment = entry.getKey();
			int level = entry.getValue();
			if(enchantment == Enchantment.ARROW_INFINITE && bender.hasAffinity(BendingAffinity.BOW)) {
				continue;
			}
			if(!item.containsEnchantment(enchantment)) {
				continue;
			}
			if(level == 0) {
				item.removeEnchantment(enchantment);
				continue;
			}
			try {
				if(item.getEnchantmentLevel(enchantment) >= level) {
					item.removeEnchantment(enchantment);
					item.addEnchantment(enchantment, level);
				}
			} catch(IllegalArgumentException e) {
				Bending.getInstance().getLogger().severe("Player "+bender.getPlayer().getName()+" had item "+item.getType()+" with enchant "+enchantment.getName()+" but... NOPE :o");
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
		
		if(item.getType() == Material.GOLDEN_APPLE 
				|| item.getType() == Material.DIAMOND_SWORD
				|| item.getType() == Material.DIAMOND_BOOTS
				|| item.getType() == Material.DIAMOND_CHESTPLATE
				|| item.getType() == Material.DIAMOND_HELMET
				|| item.getType() == Material.DIAMOND_LEGGINGS
				|| item.getType() == Material.GOLD_BOOTS
				|| item.getType() == Material.GOLD_CHESTPLATE
				|| item.getType() == Material.GOLD_HELMET
				|| item.getType() == Material.GOLD_LEGGINGS
				|| item.getType() == Material.SPECTRAL_ARROW
				|| item.getType() == Material.TIPPED_ARROW) {
			bender.getPlayer().getInventory().remove(item);
		}
		
		// Firebender might keep golden sword for FireBlade
		if(bender == null || !bender.isBender(BendingElement.FIRE)) {
			if(item.getType() == Material.GOLD_SWORD) {
				bender.getPlayer().getInventory().remove(item);
			}
		}
		
		// Airbender might keep elytra
		if(bender == null || !bender.isBender(BendingElement.AIR)) {
			if(item.getType() == Material.ELYTRA) {
				bender.getPlayer().getInventory().remove(item);
			}
		}
		
		// Swordman might keep extra shield & iron sword
		if(bender == null || !bender.hasAffinity(BendingAffinity.SWORD)) {
			if(item.getType() == Material.SHIELD 
					|| item.getType() == Material.IRON_SWORD) {
				bender.getPlayer().getInventory().remove(item);
			}
		}
		
		// Metal bender might keep iron armors
		if(bender == null || !bender.hasAffinity(BendingAffinity.METAL)) {
			if(item.getType() == Material.IRON_BOOTS 
					|| item.getType() == Material.IRON_CHESTPLATE
					|| item.getType() == Material.IRON_HELMET
					|| item.getType() == Material.IRON_LEGGINGS) {
				bender.getPlayer().getInventory().remove(item);
			}
		}
	}
}
