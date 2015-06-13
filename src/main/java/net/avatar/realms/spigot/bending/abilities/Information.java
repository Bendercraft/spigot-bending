package net.avatar.realms.spigot.bending.abilities;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class Information {

	private long time;
	private Block block;
	private Material type;
	private byte data;
	private BlockState state;

	private static int ID = Integer.MIN_VALUE;
	private int id;

	public Information() {
		id = ID++;
		if (ID >= Integer.MAX_VALUE) {
			ID = Integer.MIN_VALUE;
		}
	}

	public int getID() {
		return id;
	}

	public void setState(BlockState state) {
		this.state = state;
	}

	public BlockState getState() {
		return state;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getTime() {
		return time;
	}

	public void setBlock(Block block) {
		this.block = block;
	}

	public Block getBlock() {
		return block;
	}

	public void setType(Material type) {
		this.type = type;
	}

	public Material getType() {
		return type;
	}

	public void setData(byte data) {
		this.data = data;
	}

	public byte getData() {
		return data;
	}
	
	@SuppressWarnings("deprecation")
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
