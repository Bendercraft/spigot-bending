package net.bendercraft.spigot.bending.listeners;

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
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.earth.EarthArmor;
import net.bendercraft.spigot.bending.controller.Settings;

public class BendingDenyItem implements Listener {
	
	private Map<UUID, Long> enderpearls;
	
	private Map<Enchantment, Integer> deniedEnchantments;
	private List<PotionEffectType> deniedPotions;
	
	public BendingDenyItem() {
		enderpearls = new HashMap<UUID, Long>();
		deniedEnchantments = new HashMap<Enchantment, Integer>();
		deniedPotions = new LinkedList<PotionEffectType>();
		
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
		deniedEnchantments.put(Enchantment.MENDING, 1);
		deniedEnchantments.put(Enchantment.FROST_WALKER, 0);
		
		deniedPotions.add(PotionEffectType.INCREASE_DAMAGE);
		deniedPotions.add(PotionEffectType.FIRE_RESISTANCE);
		deniedPotions.add(PotionEffectType.INVISIBILITY);
		
		//Fool check
		Bending.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(Bending.getInstance(), new Runnable() {
			@Override
			public void run() {
				for(Player player : Bending.getInstance().getServer().getOnlinePlayers()) {
					sanitize(player);
				}
			}
		}, 100, 100);
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
		sanitize(event.getPlayer());
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
		if(bender.getPlayer().hasPermission("bending.denyitem.bypass")) {
			return;
		}
		if(item.getType() == Material.CHORUS_FRUIT) {
			removeItem(bender.getPlayer(), item);
		}
		for(Entry<Enchantment, Integer> entry : deniedEnchantments.entrySet()) {
			Enchantment enchantment = entry.getKey();
			int level = entry.getValue();
			if(enchantment == Enchantment.ARROW_INFINITE && bender.hasAffinity(BendingAffinity.BOW)) {
				continue;
			}
			if(enchantment == Enchantment.ARROW_KNOCKBACK && bender.hasAffinity(BendingAffinity.BOW)) {
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
				if(item.getEnchantmentLevel(enchantment) > level) {
					item.removeEnchantment(enchantment);
					item.addUnsafeEnchantment(enchantment, level);
				}
			} catch(IllegalArgumentException e) {
				Bending.getInstance().getLogger().severe("Player "+bender.getPlayer().getName()+" had item "+item.getType()+" with enchant "+enchantment.getName()+" but... NOPE :o");
			}
		}
		
		if(item.getType() == Material.POTION) {
			PotionMeta meta = (PotionMeta) item.getItemMeta();
			if(deniedPotions.contains(meta.getBasePotionData().getType().getEffectType())) {
				removeItem(bender.getPlayer(), item);
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
			removeItem(bender.getPlayer(), item);
		}
		
		// Firebender might keep golden sword for FireBlade
		if(bender == null || !bender.isBender(BendingElement.FIRE)) {
			if(item.getType() == Material.GOLD_SWORD) {
				removeItem(bender.getPlayer(), item);
			}
		}
		
		// Airbender might keep elytra
		if(bender == null || !bender.isBender(BendingElement.AIR)) {
			if(item.getType() == Material.ELYTRA) {
				removeItem(bender.getPlayer(), item);
			}
		}
		
		// Swordman might keep extra shield & iron sword
		if(bender == null || !bender.hasAffinity(BendingAffinity.SWORD)) {
			if(item.getType() == Material.SHIELD 
					|| item.getType() == Material.IRON_SWORD) {
				removeItem(bender.getPlayer(), item);
			}
		}
		
		// Metal bender might keep iron armors
		if(bender == null || !bender.hasAffinity(BendingAffinity.METAL)) {
			if(item.getType() == Material.IRON_BOOTS 
					|| item.getType() == Material.IRON_CHESTPLATE
					|| item.getType() == Material.IRON_HELMET
					|| item.getType() == Material.IRON_LEGGINGS) {
				removeItem(bender.getPlayer(), item);
			}
		}
		
		//EarthBender with EarthArmor in progress might keep leather armor with "EartArmor" on it
		if(bender == null || !bender.isBender(BendingElement.EARTH) || !EarthArmor.hasEarthArmor(bender.getPlayer())) {
			if(item.getType() == Material.LEATHER_BOOTS 
					|| item.getType() == Material.LEATHER_CHESTPLATE
					|| item.getType() == Material.LEATHER_HELMET
					|| item.getType() == Material.LEATHER_LEGGINGS) {
				if(EarthArmor.isArmor(item)) {
					removeItem(bender.getPlayer(), item);
				}
			}
		}
	}
	
	private void removeItem(Player player, ItemStack item) {
		if(item == null) {
			return;
		}
		player.getInventory().remove(item);
		if(item.equals(player.getInventory().getHelmet())) {
			player.getInventory().setHelmet(null);
		}
		if(item.equals(player.getInventory().getChestplate())) {
			player.getInventory().setChestplate(null);
		}
		if(item.equals(player.getInventory().getLeggings())) {
			player.getInventory().setLeggings(null);
		}
		if(item.equals(player.getInventory().getBoots())) {
			player.getInventory().setBoots(null);
		}
	}
}
