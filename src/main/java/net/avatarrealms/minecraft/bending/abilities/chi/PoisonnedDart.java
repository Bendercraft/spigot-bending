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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class PoisonnedDart {

	private static Map<Player, PoisonnedDart> instances = new HashMap<Player, PoisonnedDart>();
	private static int damage = ConfigManager.dartDamage;
	private static int range = ConfigManager.dartRange;
	
	private static final ParticleEffect VISUAL = ParticleEffect.PORTAL;
	
	private Player player;
	private Location origin;
	private Location location;
	private Vector direction;
	private List<PotionEffect> potions = null;
	
	public PoisonnedDart(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer.isOnCooldown(Abilities.PoisonnedDart)) {
			return;
		}
		
		if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.PoisonnedDart, player.getLocation())) {
			return;
		}
		
		ItemStack is = player.getItemInHand();
		if (is.getType() == Material.POTION) {
			Potion p = Potion.fromItemStack(is);
			potions = new LinkedList<PotionEffect>();
			for (PotionEffect e : p.getEffects()) {
				potions.add(e);
			}
			player.getInventory().remove(is);		
		}
		this.player = player;
		origin = player.getEyeLocation();
		direction = origin.getDirection().normalize();
		
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
		
		if (origin.distance(location) > range) {
			return false;
		}
		
		if (BlockTools.isSolid(location.getBlock())) {
			return false;
		}
		
		if (!affectAround()) {
			return false;
		}
		advanceLocation();
		return true;
	}
	
	private boolean affectAround() {
		if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.PoisonnedDart, location)) {
			return false;
		}
		
		int cptEnt = 0;
		for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(location, 1.2)) {
			boolean health = false;
			for (PotionEffect ef : potions) {
				if (ef.getType() == PotionEffectType.HEAL
						|| ef.getType() == PotionEffectType.HEALTH_BOOST
						|| ef.getType() == PotionEffectType.REGENERATION) {
					health = true;
				}
				entity.addPotionEffect(ef);
			}
			if (!health) {
				EntityTools.damageEntity(player, entity, damage);
			}
			cptEnt++;
		}
		if (cptEnt > 0) {
			return false;
		}
		PluginTools.removeSpouts(location, player);
		return true;
	}
	private void advanceLocation() {
		VISUAL.display(location, 0,0,0, 1,1);
		location = location.add(direction.clone().multiply(2));
	}
	
	public static void removeAll() {
		instances.clear();
	}
	
}
