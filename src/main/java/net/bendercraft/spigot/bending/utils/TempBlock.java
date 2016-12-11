package net.bendercraft.spigot.bending.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.BendingAbility;

public class TempBlock {
	private static Map<Block, TempBlock> instances = new HashMap<Block, TempBlock>();
	private static Map<BendingAbility, List<TempBlock>> temporaries = new HashMap<BendingAbility, List<TempBlock>>();
	private static List<TempBlock> globals = Collections.synchronizedList(new LinkedList<TempBlock>());

	private Block block;
	private BlockState state;
	private boolean bendAllowed;
	private BendingAbility ability;
	private long started;
	private long duration;
	
	public static TempBlock makeGlobal(long duration, Block block, Material newType, boolean bendAllowed) {
		return makeGlobal(duration, block, newType, (byte) 0x0, bendAllowed);
	}
	
	public static TempBlock makeGlobal(long duration, Block block, Material newType, byte newData, boolean bendAllowed) {
		return make(null, duration, block, newType, newData, bendAllowed);
	}

	public static TempBlock makeTemporary(BendingAbility ability, Block block, Material newType, boolean bendAllowed) {
		return makeTemporary(ability, block, newType, (byte) 0x0, bendAllowed);
	}
	
	public static TempBlock makeTemporary(BendingAbility ability, Block block, Material newType, byte newData, boolean bendAllowed) {
		return make(ability, 0, block, newType, newData, bendAllowed);
	}

	@SuppressWarnings("deprecation")
	private static TempBlock make(BendingAbility ability, long duration, Block block, Material newType, byte newData, boolean bendAllowed) {
		// Reuse current on if existing
		TempBlock temp = null;
		if (instances.containsKey(block)) {
			temp = instances.remove(block);
			if(temp.ability != null) {
				List<TempBlock> temps = temporaries.get(temp.ability);
				temps.remove(temp);
				if(temps.isEmpty()) {
					temporaries.remove(temp.ability);
				}
			} else {
				globals.remove(temp);
			}
		} else {
			temp = new TempBlock(block);
		}
		
		temp.bendAllowed = bendAllowed;
		temp.ability = ability;
		temp.started = System.currentTimeMillis();
		temp.duration = duration;
		
		temp.block.setType(newType, false);
		temp.block.setData(newData);
		if (temp.state.getType() == Material.FIRE) {
			temp.state.setType(Material.AIR);
		}
		
		instances.put(temp.block, temp);
		if(temp.ability != null) {
			List<TempBlock> temps = temporaries.get(temp.ability);
			if(temps == null) {
				temps = new LinkedList<TempBlock>();
				temporaries.put(temp.ability, temps);
			}
			temps.add(temp);
		} else {
			globals.add(temp);
		}
		
		return temp;
	}

	private TempBlock(Block block) {
		this.block = block;
		this.state = block.getState();
	}

	public void revertBlock() {
		state.update(true);
		
		if(ability != null) {
			List<TempBlock> temps = temporaries.get(ability);
			if(temps != null) {
				temps.remove(this);
				if(temps.isEmpty()) {
					temporaries.remove(ability);
				}
			}
		} else {
			globals.remove(this);
		}
		instances.remove(block);
	}

	public BlockState getState() {
		return this.state;
	}
	
	public boolean isBendAllowed() {
		return bendAllowed;
	}

	public Location getLocation() {
		return this.block.getLocation();
	}

	public Block getBlock() {
		return this.block;
	}
	
	public static void revertBlock(Block block) {
		TempBlock temp = instances.get(block);
		if(temp != null) {
			temp.revertBlock();
		}
	}

	public static boolean isTempBlock(Block block) {
		return instances.containsKey(block);
	}
	
	public static boolean isGlobalTempBlock(TempBlock block) {
		return globals.contains(block);
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
	
	public static void clean(BendingAbility ability) {
		List<TempBlock> temps = temporaries.get(ability);
		if(temps != null) {
			List<TempBlock> copy = new LinkedList<TempBlock>(temps);
			copy.forEach(t -> t.revertBlock());
			Bending.getInstance().getLogger().warning("Ability "+ability.getName()+" did not cleaned up its TempBlock >:(");
		}
	}

	public static TempBlock get(Block block) {
		return instances.get(block);
	}

	public static void removeAll() {
		temporaries.clear();
		globals.clear();
		List<TempBlock> toRevert = new LinkedList<TempBlock>(instances.values());
		toRevert.forEach(t -> t.revertBlock());
	}

	public static int count() {
		return instances.size();
	}
	
	public static class QueueRevert implements Runnable {

		@Override
		public void run() {
			// Clean up globals
			long now = System.currentTimeMillis();
			List<TempBlock> reverts = new LinkedList<TempBlock>();
			for(TempBlock temp : globals) {
				if(temp.started + temp.duration < now) {
					reverts.add(temp);
				}
			}
			reverts.forEach(t -> t.revertBlock());
		}
		
	}
}
