package net.avatar.realms.spigot.bending.utils;

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
	private byte newData;
	private BlockState state;

	public static TempBlock makeTemporary(Block block, Material newType) {
		return makeTemporary(block, newType, (byte) 0x0);
	}

	@SuppressWarnings("deprecation")
	public static TempBlock makeTemporary(Block block, Material newType, byte newData) {
		TempBlock temp = null;
		if (instances.containsKey(block)) {
			temp = instances.get(block);
		}
		else {
			temp = new TempBlock(block);
			instances.put(block, temp);
		}
		temp.newData = newData;
		temp.block.setType(newType);
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

	/*public TempBlock(Block block, Material newType) {
		this(block, newType, (byte) 0x0);
	}

	@SuppressWarnings("deprecation")
	public TempBlock(Block block, Material newType, byte newData) {
		this.block = block;
		this.newData = newData;
		this.newType = newType;
		if (instances.containsKey(block)) {
			TempBlock temp = instances.get(block);
			if (newType != temp.newType) {
				temp.block.setType(newType);
				temp.newType = newType;
			}
			if (newData != temp.newData) {
				temp.block.setData(newData);
				temp.newData = newData;
			}
			this.state = temp.state;
			instances.put(block, temp);
		} else {
			this.state = block.getState();
			block.setType(newType);
			block.setData(newData);
			instances.put(block, this);
		}
		if (this.state.getType() == Material.FIRE) {
			this.state.setType(Material.AIR);
		}
	}*/

	public void revertBlock() {
		this.state.update(true);
		instances.remove(this.block);
	}

	public BlockState getState() {
		return this.state;
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

	public void setType(Material material) {
		setType(material, this.newData);
	}

	@SuppressWarnings("deprecation")
	public void setType(Material material, byte data) {
		this.newData = data;
		this.block.setType(material);
		this.block.setData(data);
	}

}
