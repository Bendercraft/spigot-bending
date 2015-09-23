package net.avatar.realms.spigot.bending.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class TemporaryBlock {

	private static Map<Block, TemporaryBlock> blocks = new HashMap<Block, TemporaryBlock>();

	private Block block;
	private BlockState state;
	private long started;
	private long maxDuration;

	private TemporaryBlock(Block block, BlockState state) {
		this.block = block;
		this.state = state;
		this.started = System.currentTimeMillis();
		this.maxDuration = 0;
	}

	private TemporaryBlock(Block block, BlockState state, long maxDuration) {
		this(block, state);
		this.maxDuration = maxDuration;
	}

	public static TemporaryBlock makeTemporary(Block block) {
		if (!blocks.containsKey(block)) {
			TemporaryBlock temp = new TemporaryBlock(block, block.getState());
			blocks.put(block, temp);
			return temp;
		}
		return null;
	}

	public static TemporaryBlock makeTemporary(Block block, Material newType) {
		if (!blocks.containsKey(block)) {
			TemporaryBlock temp = new TemporaryBlock(block, block.getState());
			blocks.put(block, temp);
			block.setType(newType);
			return temp;
		}

		return null;
	}

	public static TemporaryBlock makeTemporary(Block block, long maxDuration) {
		if (!blocks.containsKey(block)) {
			TemporaryBlock temp = new TemporaryBlock(block, block.getState(), maxDuration);
			blocks.put(block, temp);
			return temp;
		}
		return null;
	}

	public static TemporaryBlock makeTemporary(Block block, Material newType, long maxDuration) {
		if (!blocks.containsKey(block)) {
			TemporaryBlock temp = new TemporaryBlock(block, block.getState(), maxDuration);
			blocks.put(block, temp);
			block.setType(newType);
			return temp;
		}

		return null;
	}

	public static boolean isTemporaryBlock(Block block) {
		return blocks.containsKey(block);
	}

	public static TemporaryBlock getTemporaryBlock(Block block) {
		if (blocks.containsKey(block)) {
			return blocks.get(block);
		}
		return null;
	}

	private boolean progress() {
		if (this.maxDuration > 0 && System.currentTimeMillis() > this.started + this.maxDuration) {
			return false;
		}
		return true;
	}

	public static void progressAll() {
		List<Block> toRemove = new LinkedList<Block>();
		for (TemporaryBlock temp : blocks.values()) {
			if (!temp.progress()) {
				toRemove.add(temp.block);
			}
		}

		for (Block block : toRemove) {
			blocks.get(block).revert();
			blocks.remove(block);
		}
	}

	public static void removeAll() {
		for (TemporaryBlock temp : blocks.values()) {
			temp.revert();
		}

		blocks.clear();
	}

	private void revert() {
		this.state.update(true);
	}

	public void forceRevert() {
		this.state.update(true);
		blocks.remove(this.block);
	}


}
