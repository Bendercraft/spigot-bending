package net.avatarrealms.minecraft.bending.abilities.water;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;

public class IceSwipe implements IAbility{
	
	private static Map<Player, IceSwipe> instances = new HashMap<Player, IceSwipe>();
	
	private static int range = ConfigManager.iceSwipeRange;
	private static int damage = ConfigManager.iceSwipeDamage;
	
	private Map<Block, Location> iceblocks;
	private List<Block> blocksAround;
	
	private IAbility parent;
	private Player player;
	private BendingPlayer bPlayer;
	//private long time;
	
	private Location origin; // Could be useless
	
	private boolean started;
	private boolean ready;

	//TODO : As Kya against Zaheer
	//http://heyitsmonikashay.tumblr.com/post/91866392965/gif-request-for-imkindaprettygay-kya-vs-zaheer
	//http://37.media.tumblr.com/3154e22530fc425125e1155e7d30d3f0/tumblr_n8pysnfjYW1rkmjxwo2_250.gif
	
	public IceSwipe(Player player, Block sourceblock, IAbility parent) {
		
		bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (bPlayer.isOnCooldown(Abilities.IceSwipe)) {
			return;
		}
		
		this.player = player;
		iceblocks = new HashMap<Block, Location>();
		started = false;
		this.parent = parent;
		//time = System.currentTimeMillis();
		origin = sourceblock.getLocation();
		
	}
	
	public static void prepare(Player player) {
		//When they click
		if (instances.containsKey(player)) {
			instances.get(player).launchBlock();
		}
		else {
			Block source = BlockTools.getWaterSourceBlock(player, range, 
					EntityTools.canPlantbend(player));
			
			if (source != null && !PluginTools.isRegionProtectedFromBuild(player, Abilities.IceSwipe, source.getLocation())) {
				new IceSwipe(player, source, null);
			}
		}	
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
		
		if (PluginTools.isRegionProtectedFromBuild(player, Abilities.IceSwipe, player.getLocation())) {
			return false;
		}
		
		if (started) {
			if (! player.isSneaking()) {
				return false;
			}
			
			if (!ready) {
				
			}
			
			if (ready) {
				manageBlocks();
			}
		}	
		return true;
	}
	
	public static void start(Player player) {
		//When they sneak
		if (instances.containsKey(player)) {
			instances.get(player).started = true;
		}
	}
	public void manageBlocks() {
		List<Block> blocksToRemove = new LinkedList<Block>();
		for (Block block : iceblocks.keySet()) {
			if (block.getLocation().distance(player.getLocation()) > range) {
				blocksToRemove.add(block);
				continue;
			}
			
			if (PluginTools.isRegionProtectedFromBuild(player, Abilities.IceSwipe, block.getLocation())) {
				blocksToRemove.add(block);
				continue;
			}
			
			// I feel it will be very laggy
			for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(block.getLocation(), 1)) {
				if (entity.getUniqueId() == player.getUniqueId()) {
					continue;	
				}
				blockHit(block, entity);
				blocksToRemove.add(block);
			}
		}
		
		for (Block block : blocksToRemove) {
			iceblocks.remove(block);
		}
	}
	
	public void moveWaterAround() {
		
	}
	
	public void blockHit(Block block, LivingEntity entity) {
		// Don't know if 'block' is going to be useful, may disappear
		
	}
	
	public void launchBlock() {
		Block waterblock = BlockTools.getWaterSourceBlock(player, range,
				EntityTools.canPlantbend(player));
		if (waterblock != null && waterblock.getType() != Material.AIR) {
			Location targetloc = EntityTools.getTargetBlock(player, range,
				BlockTools.getTransparentEarthbending()).getLocation();
			
			if (targetloc != null) {
				waterblock.setType(Material.ICE);
				iceblocks.put(waterblock, targetloc);
			}
		}
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}
