package net.avatarrealms.minecraft.bending.abilities.water;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.IAbility;

public class IceSwipe implements IAbility{
	
	private static Map<Player, IceSwipe> instances = new HashMap<Player, IceSwipe>();
	
	private static int range = ConfigManager.iceSwipeRange;
	private static int damage = ConfigManager.iceSwipeDamage;
	
	private List<Block> blocks;
	
	private IAbility parent;
	private Player player;
	private BendingPlayer bPlayer;
	private boolean started;
	private Location origin; // Could be useless
	private Location targettedLocation;
	
	//TODO : Not to forget to check for the protected region
	//TODO : As Kya against Zaheer
	
	public IceSwipe(Player player, Block sourceblock, IAbility parent) {
		
		bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (bPlayer.isOnCooldown(Abilities.IceSwipe)) {
			return;
		}
		
		this.player = player;
		blocks = new LinkedList<Block>();
		started = false;
		this.parent = parent;
		origin = sourceblock.getLocation();
		
	}
	
	public static void prepare(Player player) {
		
		
	}
	
	public static void progressAll() {
		List<Player> toRemove = new LinkedList<Player>();
		for (Player pl : instances.keySet()) {
			boolean keep = instances.get(pl).progress();
			if (!keep) {
				toRemove.add(pl);
			}
		}
		
		for (Player pl : toRemove) {
			instances.remove(pl);
		}
	}
	
	public boolean progress() {
		
		if (!player.isOnline() || player.isDead()) {
			return false;
		}
		
		if (started) {
			if (! player.isSneaking()) {
				return false;
			}
		}
		
		return true;
	}
	
	public void moveWaterAround() {
		
	}
	
	public void retargetBlocks() {
		
	}
	
	public void launchBlock() {
		
	}

	
	@Override
	public int getBaseExperience() {
		return 4;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}
