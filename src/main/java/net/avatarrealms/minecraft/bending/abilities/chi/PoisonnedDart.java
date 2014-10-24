package net.avatarrealms.minecraft.bending.abilities.chi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.ParticleEffect;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import net.avatarrealms.minecraft.bending.utils.ProtectionManager;

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
		if (is.getType() == Material.POTION) {
			effect = EntityTools.fromItemStack(is);
			player.getInventory().remove(is);		
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
		for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location, 1.65)) {
			boolean health = false;
			if (effect != null) {
				Bukkit.getLogger().info(effect.getType().getName());
				if (effect.getType() == PotionEffectType.HEAL
						|| effect.getType() == PotionEffectType.HEALTH_BOOST
						|| effect.getType() == PotionEffectType.REGENERATION) {
					health = true;
				}
				entity.addPotionEffect(effect);	
			}			
			if (!health) {
				EntityTools.damageEntity(player, entity, damage);
			}
			cptEnt++;
		}
		PluginTools.removeSpouts(location, player);
		if (cptEnt > 0) {
			return false;
		}
		return true;
	}
	private void advanceLocation() {
		VISUAL.display(location, 0,0,0, 1,1);
		location = location.add(direction.clone().multiply(1));
	}
	
	public static void removeAll() {
		instances.clear();
	}
	
}
