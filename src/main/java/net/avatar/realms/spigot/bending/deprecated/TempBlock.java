package net.avatar.realms.spigot.bending.deprecated;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;

@Deprecated
// Use BlockState instead
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
			this.state = temp.state;
			instances.put(block, temp);
		} else {
			this.state = block.getState();
			block.setType(newtype);
			block.setData(newdata);
			instances.put(block, this);
		}
		if (this.state.getType() == Material.FIRE) {
			this.state.setType(Material.AIR);
		}
	}

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
		BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN };
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
		setType(material, this.newdata);
	}

	public void setType(Material material, byte data) {
		this.newtype = material;
		this.newdata = data;
		this.block.setType(material);
		this.block.setData(data);
	}

}
