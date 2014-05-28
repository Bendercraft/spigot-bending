package net.avatarrealms.minecraft.bending.model;

import java.util.concurrent.ConcurrentHashMap;

import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;

public class TempBlock {

	public static ConcurrentHashMap<Block, TempBlock> instances = new ConcurrentHashMap<Block, TempBlock>();

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
			instances.replace(block, temp);
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
		// Tools.verbose(block.getType());
		// if (block.getType() == newtype
		// || (Tools.isWater(block) && (newtype == Material.WATER || newtype ==
		// Material.STATIONARY_WATER))) {
		// if (type == Material.WATER || type == Material.STATIONARY_WATER
		// || type == Material.AIR) {
		// if (Tools.adjacentToThreeOrMoreSources(block)) {
		// type = Material.WATER;
		// data = (byte) 0x0;
		// }
		// }
		// block.setType(type);
		// block.setData(data);
		// }
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
					&& Tools.adjacentToThreeOrMoreSources(block)) {
				block.setType(Material.WATER);
				block.setData((byte) 0x0);
			} else {
				block.setType(defaulttype);
			}
		}
		// block.setType(defaulttype);
	}

	public static void removeBlock(Block block) {
		if (instances.containsKey(block)) {
			instances.remove(block);
		}
	}

	public static boolean isTempBlock(Block block) {
		if (instances.containsKey(block))
			return true;
		return false;
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
		for (Block block : instances.keySet()) {
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
