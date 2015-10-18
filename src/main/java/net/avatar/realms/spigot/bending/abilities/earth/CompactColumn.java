package net.avatar.realms.spigot.bending.abilities.earth;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.utils.BlockTools;

public class CompactColumn {
	private static Map<Integer, CompactColumn> instances = new HashMap<Integer, CompactColumn>();

	private static int ID = Integer.MIN_VALUE;

	private static int depth = Collapse.DEPTH;

	private static final Vector direction = new Vector(0, -1, 0);

	private static long interval = (long) (1000. / Collapse.SPEED);

	private Location origin;
	private Location location;
	private Block block;
	private Player player;
	private int distance;
	private int id;
	private long time;
	private Map<Block, Block> affectedblocks = new HashMap<Block, Block>();

	public CompactColumn(Player player) {
		this.block = BlockTools.getEarthSourceBlock(player, BendingAbilities.Collapse, Collapse.RANGE);
		if (this.block == null) {
			return;
		}

		this.origin = this.block.getLocation();
		this.location = this.origin.clone();
		this.player = player;
		this.distance = BlockTools.getEarthbendableBlocksLength(player, this.block, direction.clone().multiply(-1), depth);

		loadAffectedBlocks();

		if (this.distance != 0) {
			if (canInstantiate()) {
				this.id = ID;
				instances.put(this.id, this);
				if (ID >= Integer.MAX_VALUE) {
					ID = Integer.MIN_VALUE;
				}
				ID++;
				this.time = System.currentTimeMillis() - interval;
			}
		}
	}

	public CompactColumn(Player player, Location origin) {
		// Tools.verbose("New compact column");
		this.origin = origin;
		this.player = player;
		this.block = origin.getBlock();
		// Tools.verbose(block);
		// Tools.verbose(origin);
		this.location = origin.clone();
		this.distance = BlockTools.getEarthbendableBlocksLength(player, this.block, direction.clone().multiply(-1), depth);

		loadAffectedBlocks();

		if (this.distance != 0) {
			if (canInstantiate()) {
				for (Block blocki : this.affectedblocks.keySet()) {
					EarthColumn.resetBlock(blocki);
				}
				this.id = ID;
				instances.put(this.id, this);
				if (ID >= Integer.MAX_VALUE) {
					ID = Integer.MIN_VALUE;
				}
				ID++;
				this.time = System.currentTimeMillis() - interval;
			}
		}
	}

	private void loadAffectedBlocks() {
		this.affectedblocks.clear();
		Block thisblock;
		for (int i = 0; i <= this.distance; i++) {
			thisblock = this.block.getWorld().getBlockAt(this.location.clone().add(direction.clone().multiply(-i)));
			this.affectedblocks.put(thisblock, thisblock);
			if (EarthColumn.blockInAllAffectedBlocks(thisblock)) {
				EarthColumn.revertBlock(thisblock);
			}
		}
	}

	private boolean blockInAffectedBlocks(Block block) {
		if (this.affectedblocks.containsKey(block)) {
			return true;
		}
		return false;
	}

	public static boolean blockInAllAffectedBlocks(Block block) {
		for (CompactColumn column : instances.values()) {
			if (column.blockInAffectedBlocks(block)) {
				return true;
			}
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
		for (Block block : this.affectedblocks.keySet()) {
			if (blockInAllAffectedBlocks(block)) {
				return false;
			}
		}
		return true;
	}

	public boolean progress() {
		if (System.currentTimeMillis() - this.time >= interval) {
			this.time = System.currentTimeMillis();
			if(!instances.containsKey(this.id)) {
				return false;
			}
			if (!moveEarth()) {
				for (Block blocki : this.affectedblocks.keySet()) {
					EarthColumn.resetBlock(blocki);
				}
				return false;
			}
		}
		return true;
	}

	private boolean moveEarth() {
		Block block = this.location.getBlock();
		this.location = this.location.add(direction);
		if (block == null || this.location == null || this.distance == 0) {
			return false;
		}
		BlockTools.moveEarth(this.player, block, direction, this.distance);
		loadAffectedBlocks();

		if (this.location.distance(this.origin) >= this.distance) {
			return false;
		}

		return true;
	}

	public void remove() {
		instances.remove(this.id);
	}

	public static String getDescription() {
		return " To use, simply left-click on an earthbendable block. " + "That block and the earthbendable blocks above it will be shoved " + "back into the earth below them, if they can. " + "This ability does have the capacity to trap something inside of it, " + "although it is incredibly difficult to do so. ";
	}
}
