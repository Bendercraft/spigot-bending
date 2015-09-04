package net.avatar.realms.spigot.bending.abilities.deprecated;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

@Deprecated
// Use block state instead
public class Information {

	private long time;
	private Block block;
	private Material type;
	private byte data;
	private BlockState state;

	private static int ID = Integer.MIN_VALUE;
	private int id;

	public Information() {
		this.id = ID++;
		if (ID >= Integer.MAX_VALUE) {
			ID = Integer.MIN_VALUE;
		}
	}

	public int getID() {
		return this.id;
	}

	public void setState(BlockState state) {
		this.state = state;
	}

	public BlockState getState() {
		return this.state;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getTime() {
		return this.time;
	}

	public void setBlock(Block block) {
		this.block = block;
	}

	public Block getBlock() {
		return this.block;
	}

	public void setType(Material type) {
		this.type = type;
	}

	public Material getType() {
		return this.type;
	}

	public void setData(byte data) {
		this.data = data;
	}

	public byte getData() {
		return this.data;
	}
	
	public static Information fromBlock(Block block) {
		if (block == null) {
			return null;
		}
		Information info = null;
		info = new Information();
		info.setBlock(block);
		info.setType(block.getType());
		info.setData(block.getData());
		info.setState(block.getState());
		info.setTime(System.currentTimeMillis());
		
		return info;
	}

}
