package net.avatarrealms.minecraft.bending.abilities.energy;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AstralProjection {
	public static Map<Player, AstralProjection> instances = new HashMap<Player, AstralProjection>();
	
	private Player player;
	
	public AstralProjection(Player p) {
		
		this.player = p;
		
		instances.put(p, this);
	}
	
	public boolean progress() {
		if (!player.isOnline() || player.isDead()) {
			return false;
		}
		
		if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,
													Integer.MAX_VALUE, 15));
		}
		return true;
	}
	
	public boolean isAstralProjecting(Player p) {
		return instances.containsKey(p);
	}
	
	public AstralProjection getAstralProjection(Player p) {
		return instances.get(p);
	}
	
}
