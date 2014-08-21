package net.avatarrealms.minecraft.bending.abilities.earth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.utils.BlockTools;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class CompactColumn implements IAbility {
	private static Map<Integer, CompactColumn> instances = new HashMap<Integer, CompactColumn>();
	//TODO This map never receive any elements, strange
	private static Map<Block, Block> alreadydoneblocks = new HashMap<Block, Block>();

	private static int ID = Integer.MIN_VALUE;

	private static double range = ConfigManager.compactColumnRange;
	private static int height = EarthColumn.standardheight;
	private static double speed = ConfigManager.compactColumnSpeed;
	private static final Vector direction = new Vector(0, -1, 0);

	private static long interval = (long) (1000. / speed);

	private Location origin;
	private Location location;
	private Block block;
	private Player player;
	private int distance;
	private int id;
	private long time;
	private Map<Block, Block> affectedblocks = new HashMap<Block, Block>();
	private IAbility parent;

	public CompactColumn(Player player, IAbility parent) {
		this.parent = parent;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.Collapse))
			return;

		block = BlockTools.getEarthSourceBlock(player, range);
		if (block == null) {
			return;
		}
			
		origin = block.getLocation();
		location = origin.clone();
		this.player = player;
		distance = BlockTools.getEarthbendableBlocksLength(player, block, direction
				.clone().multiply(-1), height);

		loadAffectedBlocks();

		if (distance != 0) {
			if (canInstantiate()) {
				id = ID;
				instances.put(id, this);
				bPlayer.cooldown(Abilities.Collapse);
				bPlayer.earnXP(BendingType.Earth, this);
				if (ID >= Integer.MAX_VALUE) {
					ID = Integer.MIN_VALUE;
				}
				ID++;
				time = System.currentTimeMillis() - interval;
			}
		}
	}

	public CompactColumn(Player player, Location origin, IAbility parent) {
		this.parent = parent;
		// Tools.verbose("New compact column");
		this.origin = origin;
		this.player = player;
		block = origin.getBlock();
		// Tools.verbose(block);
		// Tools.verbose(origin);
		location = origin.clone();
		distance = BlockTools.getEarthbendableBlocksLength(player, block, direction
				.clone().multiply(-1), height);

		loadAffectedBlocks();

		if (distance != 0) {
			if (canInstantiate()) {
				for (Block blocki : affectedblocks.keySet()) {
					EarthColumn.resetBlock(blocki);
				}
				id = ID;
				instances.put(id, this);
				if (ID >= Integer.MAX_VALUE) {
					ID = Integer.MIN_VALUE;
				}
				ID++;
				time = System.currentTimeMillis() - interval;
			}
		}
	}

	private void loadAffectedBlocks() {
		affectedblocks.clear();
		Block thisblock;
		for (int i = 0; i <= distance; i++) {
			thisblock = block.getWorld().getBlockAt(
					location.clone().add(direction.clone().multiply(-i)));
			affectedblocks.put(thisblock, thisblock);
			if (EarthColumn.blockInAllAffectedBlocks(thisblock))
				EarthColumn.revertBlock(thisblock);
		}
	}

	private boolean blockInAffectedBlocks(Block block) {
		if (affectedblocks.containsKey(block)) {
			return true;
		}
		return false;
	}

	public static boolean blockInAllAffectedBlocks(Block block) {
		for (CompactColumn column : instances.values()) {
			if (column.blockInAffectedBlocks(block))
				return true;
		}
		return false;
	}

	public static void revertBlock(Block block) {
		for (CompactColumn column : instances.values()) {
			if (column.blockInAffectedBlocks(block)) {
				column.affectedblocks.remove(block);
			}
		}
	}

	private boolean canInstantiate() {
		for (Block block : affectedblocks.keySet()) {
			if (blockInAllAffectedBlocks(block)
					|| alreadydoneblocks.containsKey(block)) {
				return false;
			}
		}
		return true;
	}

	private boolean progress() {
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			if (!moveEarth()) {
				for (Block blocki : affectedblocks.keySet()) {
					EarthColumn.resetBlock(blocki);
				}
				return false;
			}
		}
		return true;
	}

	private boolean moveEarth() {
		Block block = location.getBlock();
		location = location.add(direction);
		if (block == null || location == null || distance == 0) {
			return false;
		}
		BlockTools.moveEarth(player, block, direction, distance);
		loadAffectedBlocks();

		if (location.distance(origin) >= distance) {
			return false;
		}

		return true;
	}

	public static void progressAll() {
		List<CompactColumn> toRemove = new LinkedList<CompactColumn>();
		
		for(CompactColumn column : instances.values()) {
			boolean keep = column.progress();
			if(!keep) {
				toRemove.add(column);
			}
		}
		for(CompactColumn column : toRemove) {
			column.remove();
		}
	}

	private void remove() {
		instances.remove(id);
	}

	public static void removeAll() {
		instances.clear();
	}

	public static String getDescription() {
		return " To use, simply left-click on an earthbendable block. "
				+ "That block and the earthbendable blocks above it will be shoved "
				+ "back into the earth below them, if they can. "
				+ "This ability does have the capacity to trap something inside of it, "
				+ "although it is incredibly difficult to do so. ";
	}

	@Override
	public int getBaseExperience() {
		return 3;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}
