package net.avatarrealms.minecraft.bending.abilities.fire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.EntityTools;

import org.bukkit.entity.Player;

public class FireBlade {
	
	private static Map<Player, FireBlade> instances = new HashMap<Player, FireBlade>();
	private static int sharpnessLevel = ConfigManager.fireBladeSharpnessLevel;
	private static int fireAspectLevel = ConfigManager.fireBladeFireAspectLevel;
	private static int strengthLevel = ConfigManager.fireBladeStrengthLevel;
	private static int duration = ConfigManager.fireBladeDuration;
	
	private long time;
	private Player player;
	
	public FireBlade (Player player) {
		this.player = player;
		this.time = System.currentTimeMillis();
	}
	
	public void progressAll() {
		
		List<Player> toRemove = new LinkedList<Player>();
		for (Player player : instances.keySet()) {
			boolean keep = instances.get(player).progress();
			if (!keep) {
				instances.get(player).removeFireBlade();
				toRemove.add(player);
			}
		}
		
		for (Player pl : toRemove) {
			instances.remove(pl);
		}
		
	}
	
	public boolean progress() {
		
		if (System.currentTimeMillis() > time + (1000*duration)) {
			return false;
		}
		
		if (EntityTools.getBendingAbility(player) != Abilities.FireBlade) {
			return false;
		}
		return true;
	}
	
	public void removeFireBlade() {
		
	}
	
	public void gireFireBlade() {
		
	}

}
