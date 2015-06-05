package net.avatar.realms.spigot.bending.abilities.chi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.controller.ConfigManager;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ParticleEffect;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class PoisonnedDart {

	private static Map<Player, PoisonnedDart> instances = new HashMap<Player, PoisonnedDart>();
	private static int damage = ConfigManager.dartDamage;
	private static int range = ConfigManager.dartRange;
	
	private static final ParticleEffect VISUAL = ParticleEffect.HAPPY_VILLAGER;
	
	private Player player;
	private Location origin;
	private Location location;
	private Vector direction;
	private PotionEffect effect;
	
	public PoisonnedDart(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer.isOnCooldown(Abilities.PoisonnedDart)) {
			return;
		}
		
		if (instances.containsKey(player)) {
			return;
		}
		
		if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.PoisonnedDart, player.getLocation())) {
			return;
		}
		
		ItemStack is = player.getItemInHand();
		if (is.getType() == Material.MILK_BUCKET) {
			effect = null;
			is.setType(Material.BUCKET);
			is.setAmount(1);
		} else if (is.getType() == Material.POTION) {
			effect = EntityTools.fromItemStack(is);
			is.setType(Material.GLASS_BOTTLE);
			is.setAmount(1);
		} else if (is.getType() == Material.EYE_OF_ENDER) {
			effect = new PotionEffect(PotionEffectType.BLINDNESS,20*10,1);
		} else if (is.getType() == Material.MUSHROOM_SOUP) {
			effect = new PotionEffect(PotionEffectType.CONFUSION,20*12,1);
		} else {
			effect = new PotionEffect(PotionEffectType.POISON, 20*1, 0);
		}
		this.player = player;
		origin = player.getEyeLocation();
		location = origin.clone();
		direction = origin.getDirection().normalize();
		
		origin.getWorld().playSound(origin, Sound.SHOOT_ARROW, 10, 1);
		
		instances.put(player, this);
		
		bPlayer.cooldown(Abilities.PoisonnedDart);
	}
	public static void progressAll() {
		List<Player> toRemove = new LinkedList<Player>();
		for (Player p : instances.keySet()) {
			boolean keep = instances.get(p).progress();
			if (!keep) {
				toRemove.add(p);
			}
		}
		
		for (Player p : toRemove) {
			instances.remove(p);
		}
	}
	
	public boolean progress() {
		if (!player.isOnline() || player.isDead()) {
			return false;
		}
		
		if (!player.getWorld().equals(location.getWorld())) {
			return false;
		}
		if (location.distance(origin) > range) {
			return false;
		}
		
		if (BlockTools.isSolid(location.getBlock())) {
			return false;
		}
		
		advanceLocation();
		if (!affectAround()) {
			return false;
		}
		return true;
	}
	
	private boolean affectAround() {
		if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.PoisonnedDart, location)) {
			return false;
		}		
		int cptEnt = 0;
		for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location, 1.9)) {
			if (entity.getEntityId() == player.getEntityId()) {
				continue;
			}
			boolean health = false;
			if (effect != null) {
				Bukkit.getLogger().info(effect.getType().getName());
				if (effect.getType().equals(PotionEffectType.HEAL)
						|| effect.getType().equals(PotionEffectType.HEALTH_BOOST)
						|| effect.getType().equals(PotionEffectType.REGENERATION)) {
					health = true;
				}
				entity.addPotionEffect(effect);	
			}
			else {
				entity.getActivePotionEffects().clear();
			}
			if (!health) {
				EntityTools.damageEntity(player, entity, damage);
			}
			cptEnt++;
		}

		if (cptEnt > 0) {
			return false;
		}
		return true;
	}
	private void advanceLocation() {
		VISUAL.display(location, 0,0,0, 1,1);
		location = location.add(direction.clone().multiply(1.5));
	}
	
	public static void removeAll() {
		instances.clear();
	}
	
}
