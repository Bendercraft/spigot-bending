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
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.arts.Supply;
import net.bendercraft.spigot.bending.abilities.earth.EarthArmor;
import net.bendercraft.spigot.bending.controller.Settings;

public class BendingDenyItem implements Listener {
	
	private Map<UUID, Long> enderpearls;
	
	private Map<Enchantment, Integer> deniedEnchantments;
	private List<PotionEffectType> deniedPotions;
	
	public BendingDenyItem() {
		enderpearls = new HashMap<>();
		deniedEnchantments = new HashMap<>();
		deniedPotions = new LinkedList<>();

		deniedEnchantments.put(Enchantment.PROTECTION_ENVIRONMENTAL, 3); // ID : protection
		deniedEnchantments.put(Enchantment.PROTECTION_FIRE, 0); // ID : fire_protection
		deniedEnchantments.put(Enchantment.THORNS, 1); // ID : thorns
		deniedEnchantments.put(Enchantment.DAMAGE_ALL, 2); // ID : sharpness
		deniedEnchantments.put(Enchantment.KNOCKBACK, 1); // ID : knockback
		deniedEnchantments.put(Enchantment.FIRE_ASPECT, 0); // ID : fire_aspect
		deniedEnchantments.put(Enchantment.ARROW_DAMAGE, 0); // ID : power
		deniedEnchantments.put(Enchantment.ARROW_KNOCKBACK, 0); // ID : punch
		deniedEnchantments.put(Enchantment.ARROW_FIRE, 0); // ID : flame
		deniedEnchantments.put(Enchantment.ARROW_INFINITE, 0); // ID : infinity
		deniedEnchantments.put(Enchantment.FROST_WALKER, 0); // ID : frost_walker
		deniedEnchantments.put(Enchantment.SWEEPING_EDGE, 0); // ID : sweeping
		deniedEnchantments.put(Enchantment.RIPTIDE, 0); // ID : riptide
		deniedEnchantments.put(Enchantment.CHANNELING, 0); // ID : channeling
		deniedEnchantments.put(Enchantment.LOYALTY, 0); // ID : loyalty
		
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
    public void onPlayerPickupItemEvent(EntityPickupItemEvent event) {
		if(event.getEntity() instanceof Player) {
			sanitize((Player) event.getEntity());
		}
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
		if(event.hasItem()
				&& event.getMaterial() == Material.ENDER_PEARL
				&& (event.getAction() == Action.RIGHT_CLICK_AIR
					|| event.getAction() == Action.RIGHT_CLICK_BLOCK)){
			long now = System.currentTimeMillis();
			if(enderpearls.containsKey(event.getPlayer().getUniqueId())) {
				if(enderpearls.get(event.getPlayer().getUniqueId()) + Settings.ENDERPEARL_COOLDOWN > now) {
					event.setUseItemInHand(Event.Result.DENY);
				}
			}
			if(event.useItemInHand() != Event.Result.DENY) {
				enderpearls.put(event.getPlayer().getUniqueId(), now);
			}
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(event.getCause().equals(TeleportCause.CHORUS_FRUIT)) {
			event.setCancelled(true);
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
		if(item == null || bender == null || bender.getPlayer() == null) {
			return;
		}
		if(bender.getPlayer().hasPermission("bending.denyitem.bypass")) {
			return;
		}
		for(Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
			Enchantment enchantment = entry.getKey();
			int level = entry.getValue();
			Integer authorizedLevel = deniedEnchantments.get(enchantment);
			if (authorizedLevel == null) { //This enchantment is not restricted
				continue;
			}
			if (enchantment == Enchantment.ARROW_INFINITE && bender.hasAffinity(BendingAffinity.BOW)) {
				continue;
			}
			if (enchantment == Enchantment.ARROW_KNOCKBACK && bender.hasAffinity(BendingAffinity.BOW)) {
				continue;
			}
			if (authorizedLevel == 0) {
				item.removeEnchantment(enchantment);
				continue;
			}
			if (level > authorizedLevel) {
				item.addUnsafeEnchantment(enchantment, level);//If this item stack already contained the given enchantment (at any level), it will be replaced. We use the unsafe version to also restrict enchantments on unusual items.
			}
		}
		
		if(item.getType() == Material.POTION) {
			PotionMeta meta = (PotionMeta) item.getItemMeta();
			if(deniedPotions.contains(meta.getBasePotionData().getType().getEffectType())) {
				removeItem(bender.getPlayer(), item);
			}
		}
		
		if(item.getType() == Material.GOLDEN_APPLE
				|| item.getType() == Material.ENCHANTED_GOLDEN_APPLE
				|| item.getType() == Material.DIAMOND_SWORD
				|| item.getType() == Material.DIAMOND_BOOTS
				|| item.getType() == Material.DIAMOND_CHESTPLATE
				|| item.getType() == Material.DIAMOND_HELMET
				|| item.getType() == Material.DIAMOND_LEGGINGS
				|| item.getType() == Material.GOLDEN_BOOTS
				|| item.getType() == Material.GOLDEN_CHESTPLATE
				|| item.getType() == Material.GOLDEN_HELMET
				|| item.getType() == Material.GOLDEN_LEGGINGS
				|| item.getType() == Material.CHAINMAIL_BOOTS
				|| item.getType() == Material.CHAINMAIL_CHESTPLATE
				|| item.getType() == Material.CHAINMAIL_HELMET
				|| item.getType() == Material.CHAINMAIL_LEGGINGS
				|| item.getType() == Material.SPECTRAL_ARROW
				|| item.getType() == Material.TIPPED_ARROW
				|| item.getType() == Material.TOTEM_OF_UNDYING) {
			removeItem(bender.getPlayer(), item);
		}
		
		// Firebender might keep golden sword for FireBlade
		if(!bender.isBender(BendingElement.FIRE)) {
			if(item.getType() == Material.GOLDEN_SWORD) {
				removeItem(bender.getPlayer(), item);
			}
		}
		
		// Airbender might keep elytra
		if(!bender.isBender(BendingElement.AIR)) {
			if(item.getType() == Material.ELYTRA) {
				removeItem(bender.getPlayer(), item);
			}
		}
		
		// Swordman might keep extra shield & iron sword
		if(!bender.hasAffinity(BendingAffinity.SWORD)) {
			if(item.getType() == Material.SHIELD 
					|| item.getType() == Material.IRON_SWORD) {
				removeItem(bender.getPlayer(), item);
			}
		}
		
		// Remove bow generated from bowman
		if(!bender.hasAffinity(BendingAffinity.BOW)) {
			if(item.getType() == Material.BOW
					&& item.getItemMeta() != null 
					&& item.getItemMeta().hasLore() 
					&& item.getItemMeta().getLore().contains(Supply.LORE_BOW)) {
				removeItem(bender.getPlayer(), item);
			}
		}
		
		// Remove arrow generated from bowman
		if(!bender.hasAffinity(BendingAffinity.BOW)) {
			if(item.getType() == Material.ARROW
					&& item.getItemMeta() != null 
					&& item.getItemMeta().hasLore() 
					&& item.getItemMeta().getLore().contains(Supply.LORE_ARROW)) {
				removeItem(bender.getPlayer(), item);
			}
		}
		
		// Metal bender might keep iron armors
		if(!bender.hasAffinity(BendingAffinity.METAL) || !EarthArmor.hasEarthArmor(bender.getPlayer())) {
			if(item.getType() == Material.IRON_BOOTS 
					|| item.getType() == Material.IRON_CHESTPLATE
					|| item.getType() == Material.IRON_HELMET
					|| item.getType() == Material.IRON_LEGGINGS) {
				removeItem(bender.getPlayer(), item);
			}
		}
		
		//EarthBender with EarthArmor in progress might keep leather armor with "EartArmor" on it
		if(!bender.isBender(BendingElement.EARTH) || !EarthArmor.hasEarthArmor(bender.getPlayer())) {
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
