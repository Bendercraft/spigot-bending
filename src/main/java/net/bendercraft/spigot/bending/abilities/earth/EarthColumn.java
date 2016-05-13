package net.bendercraft.spigot.bending.abilities.earth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.BlockTools;

public class EarthColumn {
	private static Map<Integer, EarthColumn> instances = new HashMap<Integer, EarthColumn>();

	@ConfigurationParameter("Height")
	public static int HEIGHT = 6;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 1000;

	@ConfigurationParameter("Range")
	private static double RANGE = 20;

	@ConfigurationParameter("Speed")
	private static double SPEED = 8;

	private static final Vector direction = new Vector(0, 1, 0);

	private static long interval = (long) (1000. / SPEED);

	private static int ID = Integer.MIN_VALUE;

	private static Map<Block, Block> alreadyDoneBlocks = new HashMap<Block, Block>();
	private static Map<Block, Integer> baseBlocks = new HashMap<Block, Integer>();

	private Location origin;
	private Location location;
	private Block block;
	private int distance;
	private Player player;
	private int id;
	private long time;
	private int height = HEIGHT;
	private List<Block> affectedBlocks = new ArrayList<Block>();
	private final RegisteredAbility earthRegister;

	public EarthColumn() {
		this.earthRegister = AbilityManager.getManager().getRegisteredAbility(EarthWall.NAME);
	}

	public boolean init(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(EarthWall.NAME)) {
			return false;
		}
		block = BlockTools.getEarthSourceBlock(player, earthRegister, RANGE);
		if (block == null)
			return  false;
		origin = block.getLocation();
		location = origin.clone();
		distance = BlockTools.getEarthbendableBlocksLength(player, block, direction.clone().multiply(-1), height);

		this.player = player;

		loadAffectedBlocks();

		if (distance != 0) {
			if (canInstantiate()) {
				id = ID;
				instances.put(id, this);
				bPlayer.cooldown(EarthWall.NAME, COOLDOWN);
				if (ID >= Integer.MAX_VALUE) {
					ID = Integer.MIN_VALUE;
				}
				ID++;
				time = System.currentTimeMillis() - interval;
				return true;
			}
		}
		return true;
	}

	public boolean init(Player player, Location origin, int height) {
		this.height = height;
		this.origin = origin;
		location = origin.clone();
		block = location.getBlock();
		this.player = player;
		distance = BlockTools.getEarthbendableBlocksLength(player, block, direction.clone().multiply(-1), height);
		loadAffectedBlocks();

		if (distance != 0) {
			if (canInstantiate()) {
				id = ID;
				instances.put(id, this);
				if (ID >= Integer.MAX_VALUE) {
					ID = Integer.MIN_VALUE;
				}
				ID++;
				time = System.currentTimeMillis() - interval;
				return true;
			}
		}
		return false;
	}

	private void loadAffectedBlocks() {
		affectedBlocks.clear();
		Block thisBlock;
		for (int i = 0; i <= distance; i++) {
			thisBlock = block.getWorld().getBlockAt(location.clone().add(direction.clone().multiply(-i)));
			if (thisBlock.getType() != Material.ANVIL) {
				affectedBlocks.add(thisBlock);
			}
			if (CompactColumn.blockInAllAffectedBlocks(thisBlock))
				CompactColumn.revertBlock(thisBlock);
		}
	}

	public boolean blockInAffectedBlocks(Block block) {
		if (affectedBlocks.contains(block)) {
			return true;
		}
		return false;
	}

	public static boolean blockInAllAffectedBlocks(Block block) {
		for (EarthColumn column : instances.values()) {
			if (column.blockInAffectedBlocks(block))
				return true;
		}
		return false;
	}

	public static void revertBlock(Block block) {
		for (EarthColumn column : instances.values()) {
			if (column.blockInAffectedBlocks(block)) {
				column.affectedBlocks.remove(block);
			}
		}
	}

	private boolean canInstantiate() {
		for (Block block : affectedBlocks) {
			if (blockInAllAffectedBlocks(block) || alreadyDoneBlocks.containsKey(block)) {
				return false;
			}
		}
		return true;
	}

	public boolean progress() {
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			if (!moveEarth()) {
				for (Block block : affectedBlocks) {
					alreadyDoneBlocks.put(block, block);
				}
				baseBlocks.put(location.clone().add(direction.clone().multiply(-1 * (distance - 1))).getBlock(), (distance - 1));

				return false;
			}
		}
		return true;
	}

	private boolean moveEarth() {
		Block block = location.getBlock();
		location = location.add(direction);
		BlockTools.moveEarth(player, block, direction, distance);
		loadAffectedBlocks();

		if (location.distance(origin) >= distance) {
			return false;
		}

		return true;
	}

	public void remove() {
		instances.remove(id);
	}

	public static void resetBlock(Block block) {
		if (alreadyDoneBlocks.containsKey(block)) {
			alreadyDoneBlocks.remove(block);
		}
	}
}
