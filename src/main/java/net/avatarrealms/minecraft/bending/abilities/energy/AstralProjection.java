package net.avatarrealms.minecraft.bending.abilities.energy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AstralProjection {
	public static Map<Player, AstralProjection> instances = new HashMap<Player, AstralProjection>();
	
	private Player player;
	private int foodLevel;
	
	public AstralProjection(Player p) {
		
		if (instances.containsKey(p)) {
			instances.get(p).removeEffect();
			instances.remove(p);
			return;
			
		}
		if (Tools.isRegionProtectedFromBuild(p, Abilities.AstralProjection, p.getLocation())) {
			return;
		}
		
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(p);
		
		if (bPlayer.isOnCooldown(Abilities.AstralProjection)) {
			return;
		}
		
		this.player = p;
		foodLevel = p.getFoodLevel();
		instances.put(p, this);
		player.setCustomNameVisible(false);
		
		bPlayer.cooldown(Abilities.AstralProjection);
	}
	
	public static void progressAll() {
		boolean keep;
		List<Player> toRemove = new LinkedList<Player>();
		for (Player p : instances.keySet()) {
			keep = instances.get(p).progress();
			if (!keep) {
				toRemove.add(p);
				instances.get(p).removeEffect();
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
		
		if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,
													Integer.MAX_VALUE, 15));
		}
		
		if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
													Integer.MAX_VALUE, 2));
		}
		
		if (!player.hasPotionEffect(PotionEffectType.JUMP)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP,
													Integer.MAX_VALUE, 2));
		}
		
		if (player.getFoodLevel() < foodLevel) {
			player.setFoodLevel(foodLevel);
		}
		return true;
	}
	
	public void removeEffect() {
		player.setCustomNameVisible(true);
		if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
			player.removePotionEffect(PotionEffectType.INVISIBILITY);
		}
		
		if (player.hasPotionEffect(PotionEffectType.SPEED)) {
			player.removePotionEffect(PotionEffectType.SPEED);
		}
		
		if (player.hasPotionEffect(PotionEffectType.JUMP)) {
			player.removePotionEffect(PotionEffectType.JUMP);
		}
		
	}
	
	public static void removeAll() {
		for (Player p : instances.keySet()) {
			instances.get(p).removeEffect();
		}
		
		instances.clear();
	}
	
	public static boolean isAstralProjecting(Player p) {
		return instances.containsKey(p);
	}
	
	public static AstralProjection getAstralProjection(Player p) {
		return instances.get(p);
	}
	
}
