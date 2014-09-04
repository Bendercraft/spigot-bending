package net.avatarrealms.minecraft.bending.abilities.fire;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.water.Plantbending;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class FireStream implements IAbility {
	private static Map<Integer, FireStream> instances = new HashMap<Integer, FireStream>();
	private static Map<Block, Player> ignitedblocks = new HashMap<Block, Player>();
	private static Map<Block, Long> ignitedtimes = new HashMap<Block, Long>();
	static final long soonesttime = Tools.timeinterval;

	public static int firedamage = 3;
	public static int tickdamage = 2;

	private static int ID = Integer.MIN_VALUE;
	private static double speed = ConfigManager.fireStreamSpeed;
	private static long interval = (long) (1000. / speed);

	private static long dissipateAfter = ConfigManager.dissipateAfter;

	private Player player;
	private Location origin;
	private Location location;
	private Vector direction;
	private int id;
	private long time;
	private double range;
	private IAbility parent;

	public FireStream(Location location, Vector direction, Player player,
			int range, IAbility parent) {
		this.parent = parent;
		this.range = PluginTools.firebendingDayAugment(range, player.getWorld());
		this.player = player;
		origin = location.clone();
		this.location = origin.clone();
		this.direction = direction.clone();
		this.direction.setY(0);
		this.direction = this.direction.clone().normalize();
		this.location = this.location.clone().add(this.direction);
		id = ID;
		if (ID >= Integer.MAX_VALUE) {
			ID = Integer.MIN_VALUE;
		}
		ID++;
		time = System.currentTimeMillis();
		instances.put(id, this);
	}

	public boolean progress() {
		if (Tools.isRegionProtectedFromBuild(player, Abilities.Blaze, location)) {
			return false;
		}
		if (System.currentTimeMillis() - time >= interval) {
			location = location.clone().add(direction);
			time = System.currentTimeMillis();
			if (location.distance(origin) > range) {
				return false;
			}
			Block block = location.getBlock();
			if (isIgnitable(player, block)) {
				ignite(block);
				return true;
			} else if (isIgnitable(player, block.getRelative(BlockFace.DOWN))) {
				ignite(block.getRelative(BlockFace.DOWN));
				location = block.getRelative(BlockFace.DOWN).getLocation();
				return true;
			} else if (isIgnitable(player, block.getRelative(BlockFace.UP))) {
				ignite(block.getRelative(BlockFace.UP));
				location = block.getRelative(BlockFace.UP).getLocation();
				return true;
			} else {
				return false;
			}

		}
		return true;
	}

	private void ignite(Block block) {
		if (BlockTools.isPlant(block)) {
			new Plantbending(block, this);
		}
		block.setType(Material.FIRE);
		ignitedblocks.put(block, this.player);
		ignitedtimes.put(block, System.currentTimeMillis());
	}

	public static boolean isIgnitable(Player player, Block block) {
		if (Tools.isRegionProtectedFromBuild(player, Abilities.Blaze,
				block.getLocation()))
			return false;

		Material[] overwriteable = { Material.SAPLING, Material.LONG_GRASS,
				Material.DEAD_BUSH, Material.YELLOW_FLOWER, Material.RED_ROSE,
				Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.FIRE,
				Material.SNOW, Material.TORCH };

		if (Arrays.asList(overwriteable).contains(block.getType())) {
			return true;
		} else if (block.getType() != Material.AIR) {
			return false;
		}

		Material[] ignitable = { Material.BEDROCK, Material.BOOKSHELF,
				Material.BRICK, Material.CLAY, Material.CLAY_BRICK,
				Material.COAL_ORE, Material.COBBLESTONE, Material.DIAMOND_ORE,
				Material.DIAMOND_BLOCK, Material.DIRT, Material.ENDER_STONE,
				Material.GLOWING_REDSTONE_ORE, Material.GOLD_BLOCK,
				Material.GRAVEL, Material.GRASS, Material.HUGE_MUSHROOM_1,
				Material.HUGE_MUSHROOM_2, Material.LAPIS_BLOCK,
				Material.LAPIS_ORE, Material.LOG, Material.MOSSY_COBBLESTONE,
				Material.MYCEL, Material.NETHER_BRICK, Material.NETHERRACK,
				Material.OBSIDIAN, Material.REDSTONE_ORE, Material.SAND,
				Material.SANDSTONE, Material.SMOOTH_BRICK, Material.STONE,
				Material.SOUL_SAND, Material.SNOW_BLOCK, Material.WOOD,
				Material.WOOL, Material.LEAVES };

		Block belowblock = block.getRelative(BlockFace.DOWN);
		if (Arrays.asList(ignitable).contains(belowblock.getType())) {
			return true;
		}

		return false;
	}

	private void remove() {
		instances.remove(id);
	}
	
	public static void removeAll() {
		List<Block> toRemove = new LinkedList<Block>(ignitedblocks.keySet());
		for (Block block : toRemove) {
			remove(block);
		}
	}

	public static void removeAllNoneFireIgnitedBlock() {
		List<Block> toRemove = new LinkedList<Block>(ignitedblocks.keySet());
		for (Block block : toRemove) {
			if(block.getType() != Material.FIRE) {
				remove(block);
			}
		}
	}

	public static void dissipateAll() {
		if (dissipateAfter != 0) {
			List<Block> toRemove = new LinkedList<Block>(ignitedtimes.keySet());
			for (Block block : toRemove) {
				if (block.getType() != Material.FIRE) {
					remove(block);
				} else {
					long time = ignitedtimes.get(block);
					if (System.currentTimeMillis() > time + dissipateAfter) {
						block.setType(Material.AIR);
						remove(block);
					}
				}
			}
		}
	}

	public static void progressAll() {
		List<FireStream> toRemove = new LinkedList<FireStream>();
		for(FireStream stream : instances.values()) {
			boolean keep = stream.progress();
			if(!keep) {
				toRemove.add(stream);
			}
		}
		for(FireStream stream : toRemove) {
			stream.remove();
		}
	}

	public static String getDescription() {
		return "This ability no longer exists.";
	}

	public static void remove(Block block) {
		if (ignitedblocks.containsKey(block)) {
			ignitedblocks.remove(block);
		}
		if (ignitedtimes.containsKey(block)) {
			ignitedtimes.remove(block);
		}

	}

	public static void removeAroundPoint(Location location, double radius) {
		List<FireStream> toRemove = new LinkedList<FireStream>();
		for (FireStream stream : instances.values()) {
			if (stream.location.getWorld().equals(location.getWorld()))
				if (stream.location.distance(location) <= radius)
					toRemove.add(stream);
		}
		for(FireStream stream : toRemove) {
			stream.remove();
		}
	}
	
	public static void addIgnitedBlock(Block block, Player player, long time) {
		ignitedblocks.put(block, player);
		ignitedtimes.put(block, time);
	}
	
	public static boolean isIgnited(Block block) {
		return ignitedblocks.containsKey(block);
	}

	public static Player getIgnited(Block block) {
		return ignitedblocks.get(block);
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}
