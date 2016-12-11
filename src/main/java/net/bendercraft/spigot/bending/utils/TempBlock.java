package net.bendercraft.spigot.bending.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;

public class TempBlock {
	private static Map<Block, TempBlock> instances = new HashMap<Block, TempBlock>();

	private Block block;
	private BlockState state;
	private boolean bendAllowed;

	public static TempBlock makeTemporary(Block block, Material newType, boolean bendAllowed) {
		return makeTemporary(block, newType, (byte) 0x0, bendAllowed);
	}

	@SuppressWarnings("deprecation")
	public static TempBlock makeTemporary(Block block, Material newType, byte newData, boolean bendAllowed) {
		TempBlock temp = null;
		if (instances.containsKey(block)) {
			temp = instances.get(block);
		} else {
			temp = new TempBlock(block);
			instances.put(block, temp);
		}
		temp.bendAllowed = bendAllowed;
		temp.block.setType(newType, false);
		temp.block.setData(newData);
		if (temp.state.getType() == Material.FIRE) {
			temp.state.setType(Material.AIR);
		}
		return temp;
	}

	private TempBlock(Block block) {
		this.block = block;
		this.state = block.getState();
	}

	public void revertBlock() {
		this.state.update(true);
		instances.remove(this.block);
	}

	public BlockState getState() {
		return this.state;
	}
	
	public boolean isBendAllowed() {
		return bendAllowed;
	}

	public static void revertBlock(Block block) {
		if (instances.containsKey(block)) {
			instances.get(block).revertBlock();
		}
	}

	public static boolean isTempBlock(Block block) {
		return instances.containsKey(block);
	}

	public static boolean isTouchingTempBlock(Block block) {
		BlockFace[] faces = {
				BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN };
		for (BlockFace face : faces) {
			if (instances.containsKey(block.getRelative(face))) {
				return true;
			}
		}
		return false;
	}

	public static TempBlock get(Block block) {
		return instances.get(block);
	}

	public Location getLocation() {
		return this.block.getLocation();
	}

	public Block getBlock() {
		return this.block;
	}

	public static void removeAll() {
		List<TempBlock> toRevert = new LinkedList<TempBlock>(instances.values());
		for (TempBlock block : toRevert) {
			block.revertBlock();
		}
	}

	public static int count() {
		return instances.size();
	}
}
