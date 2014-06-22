package net.avatarrealms.minecraft.bending.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatarrealms.minecraft.bending.utils.BlockTools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;

public class TempBlock {
	private static Map<Block, TempBlock> instances = new HashMap<Block, TempBlock>();

	private Block block;
	private Material newtype;
	private byte newdata;
	private BlockState state;

	public TempBlock(Block block, Material newtype, byte newdata) {
		this.block = block;
		this.newdata = newdata;
		this.newtype = newtype;
		if (instances.containsKey(block)) {
			TempBlock temp = instances.get(block);
			if (newtype != temp.newtype) {
				temp.block.setType(newtype);
				temp.newtype = newtype;
			}
			if (newdata != temp.newdata) {
				temp.block.setData(newdata);
				temp.newdata = newdata;
			}
			state = temp.state;
			instances.put(block, temp);
		} else {
			state = block.getState();
			block.setType(newtype);
			block.setData(newdata);
			instances.put(block, this);
		}
		if (state.getType() == Material.FIRE)
			state.setType(Material.AIR);
	}

	public void revertBlock() {
		state.update(true);
		instances.remove(block);
	}
	
	public BlockState getState() {
		return state;
	}

	public static void revertBlock(Block block, Material defaulttype) {
		if (instances.containsKey(block)) {
			instances.get(block).revertBlock();
		} else {
			if ((defaulttype == Material.WATER
					|| defaulttype == Material.STATIONARY_WATER || defaulttype == Material.AIR)
					&& BlockTools.adjacentToThreeOrMoreSources(block)) {
				block.setType(Material.WATER);
				block.setData((byte) 0x0);
			} else {
				block.setType(defaulttype);
			}
		}
		// block.setType(defaulttype);
	}

	public static void removeBlock(Block block) {
		instances.remove(block);
	}

	public static boolean isTempBlock(Block block) {
		return instances.containsKey(block);
	}

	public static boolean isTouchingTempBlock(Block block) {
		BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST,
				BlockFace.WEST, BlockFace.UP, BlockFace.DOWN };
		for (BlockFace face : faces) {
			if (instances.containsKey(block.getRelative(face)))
				return true;
		}
		return false;
	}

	public static TempBlock get(Block block) {
		if (isTempBlock(block))
			return instances.get(block);
		return null;
	}

	public Location getLocation() {
		return block.getLocation();
	}

	public Block getBlock() {
		return block;
	}

	public static void removeAll() {
		List<Block> toRevert = new LinkedList<Block>();
		for (Block block : instances.keySet()) {
			toRevert.add(block);
		}
		for (Block block : toRevert) {
			revertBlock(block, Material.AIR);
		}
	}

	public void setType(Material material) {
		setType(material, newdata);
	}

	public void setType(Material material, byte data) {
		newtype = material;
		newdata = data;
		block.setType(material);
		block.setData(data);
	}

}
