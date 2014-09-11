package net.avatarrealms.minecraft.bending.abilities.chi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.ParticleEffect;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Location;
import org.bukkit.entity.Player;
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
	
	public PoisonnedDart(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer.isOnCooldown(Abilities.PoisonnedDart)) {
			return;
		}
		
		if (Tools.isRegionProtectedFromBuild(player, Abilities.PoisonnedDart, player.getLocation())) {
			return;
		}
		
		this.player = player;
		origin = player.getEyeLocation();
		direction = origin.getDirection().normalize();
		
		instances.put(player, this);
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
		
		
		affectAround();
		advanceLocation();
		return true;
	}
	
	private void affectAround() {
		if (Tools.isRegionProtectedFromBuild(player, Abilities.PoisonnedDart, location)) {
			return;
		}
	}
	private void advanceLocation() {
		VISUAL.display(location, 0,0,0, 1,1);
		location = location.add(direction.clone().multiply(2));
	}
	
	public static void removeAll() {
		instances.clear();
	}
	
}
