package net.bendercraft.spigot.bending.abilities.water;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.utils.BlockTools;
import net.bendercraft.spigot.bending.utils.ProtectionManager;
import net.bendercraft.spigot.bending.utils.TempBlock;

public class Frozen {
	private static List<Frozen> instances = new LinkedList<Frozen>();
	
	public static int DURATION = 60000;
	public static int STEP = 20;
	public static int DEPTH = 8;
	
	private Player player;
	private Map<Integer, List<Block>> expands = new HashMap<Integer, List<Block>>();
	private int iteration = 0;
	private long time;
	private long interval = (long) (1000. / 8);
	private double range;
	private Location location;
	
	private Frozen(Player player, Location location, double range) {
		this.player = player;
		this.location = location;
		this.range = range;
	}

	private boolean prepareBlocks() {
		for(Block block : BlockTools.getBlocksAroundPoint(location, range)) {
			if(block.getLocation().getBlockY() < location.getBlockY() - DEPTH) {
				continue;
			}
			if(ProtectionManager.isRegionProtectedFromBendingPassives(player, block.getLocation())) {
				continue;
			}
			// List of authorized blocks to be changed
			if(block.getType() != Material.DIRT 
					&& block.getType() != Material.GRASS 
					&& block.getType() != Material.STONE
					&& block.getType() != Material.COBBLESTONE 
					&& block.getType() != Material.GRAVEL
					&& block.getType() != Material.SAND
					&& block.getType() != Material.SANDSTONE
					&& block.getType() != Material.BRICK
					&& block.getType() != Material.LEAVES
					&& block.getType() != Material.LEAVES_2) {
				continue;
			}
			int distance = (int) location.distance(block.getLocation());
			if(!expands.containsKey(distance)) {
				expands.put(distance, new LinkedList<Block>());
			}
			expands.get(distance).add(block);
		}
		for(List<Block> blocks : expands.values()) {
			Collections.shuffle(blocks);
		}
		time = System.currentTimeMillis();
		return false;
	}

	@SuppressWarnings("deprecation")
	private boolean progress() {
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			if(expands.isEmpty()) {
				return false;
			}
			if(expands.containsKey(iteration)) {
				int done = 0;
				Iterator<Block> it = expands.get(iteration).iterator();
				while(it.hasNext()) {
					Block block = it.next();
					if(block.getType() == Material.LEAVES || block.getType() == Material.LEAVES_2) {
						Material mat = Material.STAINED_GLASS;
						double rand = Math.random();
						if(rand < 0.5) {
							mat = Material.STAINED_GLASS_PANE;
						}
						
						DyeColor color = DyeColor.BLUE;
						rand = Math.random();
						if(rand < 0.5) {
							color = DyeColor.LIGHT_BLUE;
						}
						TempBlock.makeGlobal(DURATION, block, mat, color.getDyeData(), true);
						block.getWorld().playSound(block.getLocation(), Sound.BLOCK_SNOW_PLACE, 1.0f, 1.0f);
					} else {
						TempBlock.makeGlobal(DURATION, block,  Material.ICE, true);
						block.getWorld().playSound(block.getLocation(), Sound.BLOCK_SNOW_PLACE, 1.0f, 1.0f);
					}
					it.remove();
					done++;
					if(done > STEP) {
						break;
					}
				}
				if(expands.get(iteration).isEmpty()) {
					expands.remove(iteration);
					iteration++;
				}
			} else {
				iteration++;
			}
		}
		return true;
	}
	
	public static void freeze(Player player, Location location, double range) {
		Frozen frozen = new Frozen(player, location, range);
		frozen.prepareBlocks();
		instances.add(frozen);
	}
	
	public static void handle() {
		Iterator<Frozen> it = instances.iterator();
		while(it.hasNext()) {
			Frozen frozen = it.next();
			if(!frozen.progress()) {
				it.remove();
			}
		}
	}
}
