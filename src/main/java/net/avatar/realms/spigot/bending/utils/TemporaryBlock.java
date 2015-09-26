package net.avatar.realms.spigot.bending.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

/**
 * Class that handles temporary blocks for bending purposes
 *
 * @author Noko
 */
public class TemporaryBlock {
	
	/**
	 * These blocks are maintained here for the method {@link #isTemporaryBlock(Block)
	 * isTempBlock()} only. The ability that use this temp block must handle the revert itself.
	 */
	private static Map<Block, TemporaryBlock> blocks = new HashMap<Block, TemporaryBlock>();
	/**
	 * These blocks are maintained here for the {@link #progressAll()} method that will
	 * automatically delete the temporary block.
	 */
	private static Map<Block, TemporaryBlock> timedBlocks = new HashMap<Block, TemporaryBlock>();
	
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
	
	/**
	 * Create and register a temporary block
	 *
	 * @param block
	 *        The block whose you must save the state
	 * @return
	 * 		The TemporaryBlock that you will have to revert later
	 */
	public static TemporaryBlock makeTemporary(Block block) {
		if (!timedBlocks.containsKey(block)) {
			TemporaryBlock temp = new TemporaryBlock(block, block.getState());
			blocks.put(block, temp);
			return temp;
		}
		return null;
	}
	
	public static TemporaryBlock makeTemporary(Block block, Material newType) {
		if (!timedBlocks.containsKey(block)) {
			TemporaryBlock temp = new TemporaryBlock(block, block.getState());
			blocks.put(block, temp);
			block.setType(newType);
			return temp;
		}
		return null;
	}
	
	public static TemporaryBlock makeTemporary(Block block, long maxDuration) {
		if (!timedBlocks.containsKey(block)) {
			TemporaryBlock temp = new TemporaryBlock(block, block.getState(), maxDuration);
			timedBlocks.put(block, temp);
			return temp;
		}
		return null;
	}
	
	public static TemporaryBlock makeTemporary(Block block, Material newType, long maxDuration) {
		if (!timedBlocks.containsKey(block)) {
			TemporaryBlock temp = new TemporaryBlock(block, block.getState(), maxDuration);
			timedBlocks.put(block, temp);
			block.setType(newType);
			return temp;
		}
		
		return null;
	}
	
	public static boolean isTemporaryBlock(Block block) {
		if (blocks.containsKey(block)) {
			return true;
		}
		
		return timedBlocks.containsKey(block);
	}
	
	public static TemporaryBlock getTemporaryBlock(Block block) {
		if (blocks.containsKey(block)) {
			return blocks.get(block);
		}

		if (timedBlocks.containsKey(block)) {
			return timedBlocks.get(block);
		}
		return null;
	}
	
	private boolean progress() {
		if ((this.maxDuration > 0) && (System.currentTimeMillis() > (this.started + this.maxDuration))) {
			return false;
		}
		return true;
	}
	
	public static void progressAll() {
		List<Block> toRemove = new LinkedList<Block>();
		for (TemporaryBlock temp : timedBlocks.values()) {
			if (!temp.progress()) {
				toRemove.add(temp.block);
			}
		}
		
		for (Block block : toRemove) {
			timedBlocks.get(block).revert();
			timedBlocks.remove(block);
		}
	}
	
	public static void removeAll() {
		for (TemporaryBlock temp : timedBlocks.values()) {
			temp.revert();
		}
		
		timedBlocks.clear();
	}
	
	private void revert() {
		this.state.update(true);
	}
	
	public void forceRevert() {
		this.state.update(true);
		blocks.remove(this.block);
	}
}
