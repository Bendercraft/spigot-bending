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
import net.bendercraft.spigot.bending.abilities.fire.FireBlade;
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
			Long lastUse = enderpearls.get(event.getPlayer().getUniqueId());
			if(lastUse != null && lastUse + Settings.ENDERPEARL_COOLDOWN > now) {
				event.setUseItemInHand(Event.Result.DENY);
			} else {
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
			int level = entry.getValue();
			if (level > authorizedLevel) {
				item.addUnsafeEnchantment(enchantment, authorizedLevel);//If this item stack already contained the given enchantment (at any level), it will be replaced. We use the unsafe version to also restrict enchantments on unusual items.
			}
		}

		Material type = item.getType();
		if(type == Material.POTION) {
			PotionMeta meta = (PotionMeta) item.getItemMeta();
			if(deniedPotions.contains(meta.getBasePotionData().getType().getEffectType())) {
				removeItem(bender.getPlayer(), item);
				return;
			}
		}
		
		if(type == Material.GOLDEN_APPLE
				|| type == Material.ENCHANTED_GOLDEN_APPLE
				|| type == Material.DIAMOND_SWORD
				|| type == Material.DIAMOND_BOOTS
				|| type == Material.DIAMOND_CHESTPLATE
				|| type == Material.DIAMOND_HELMET
				|| type == Material.DIAMOND_LEGGINGS
				|| type == Material.GOLDEN_BOOTS
				|| type == Material.GOLDEN_CHESTPLATE
				|| type == Material.GOLDEN_HELMET
				|| type == Material.GOLDEN_LEGGINGS
				|| type == Material.CHAINMAIL_BOOTS
				|| type == Material.CHAINMAIL_CHESTPLATE
				|| type == Material.CHAINMAIL_HELMET
				|| type == Material.CHAINMAIL_LEGGINGS
				|| type == Material.SPECTRAL_ARROW
				|| type == Material.TIPPED_ARROW
				|| type == Material.TOTEM_OF_UNDYING) {
			removeItem(bender.getPlayer(), item);
			return;
		}
		
		// Firebender might keep golden sword for FireBlade
		if(!bender.isBender(BendingElement.FIRE) || !FireBlade.isFireBlading(bender.getPlayer())) {
			if(type == Material.GOLDEN_SWORD) {
				removeItem(bender.getPlayer(), item);
				return;
			}
		}
		
		// Airbender might keep elytra
		if(!bender.isBender(BendingElement.AIR)) {
			if(type == Material.ELYTRA) {
				removeItem(bender.getPlayer(), item);
				return;
			}
		}
		
		// Swordman might keep extra shield & iron sword
		if(!bender.hasAffinity(BendingAffinity.SWORD)) {
			if(type == Material.SHIELD
					|| type == Material.IRON_SWORD) {
				removeItem(bender.getPlayer(), item);
				return;
			}
		}
		
		// Remove bow and arrow generated from bowman
		if(!bender.hasAffinity(BendingAffinity.BOW)) {
			if(Supply.isBow(item)
				|| Supply.isArrow(item)) {
				removeItem(bender.getPlayer(), item);
				return;
			}
		}
		
		// Metal bender might keep iron armors
		if(!bender.hasAffinity(BendingAffinity.METAL) || !EarthArmor.hasEarthArmor(bender.getPlayer())) {
			if(type == Material.IRON_BOOTS
					|| type == Material.IRON_CHESTPLATE
					|| type == Material.IRON_HELMET
					|| type == Material.IRON_LEGGINGS) {
				removeItem(bender.getPlayer(), item);
				return;
			}
		}
		
		//EarthBender with EarthArmor in progress might keep leather armor with "EartArmor" on it
		if(!bender.isBender(BendingElement.EARTH) || !EarthArmor.hasEarthArmor(bender.getPlayer())) {
			if(type == Material.LEATHER_BOOTS
					|| type == Material.LEATHER_CHESTPLATE
					|| type == Material.LEATHER_HELMET
					|| type == Material.LEATHER_LEGGINGS) {
				if(EarthArmor.isArmor(item)) {
					removeItem(bender.getPlayer(), item);
					return;
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
