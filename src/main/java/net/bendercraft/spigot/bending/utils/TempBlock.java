package net.bendercraft.spigot.bending.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.BendingAbility;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.Lootable;


public class TempBlock {
	private static Map<Block, TempBlock> instances = new HashMap<>();
	private static Reference2ObjectMap<BendingAbility, List<TempBlock>> temporaries = new Reference2ObjectOpenHashMap<>();
	//private static Map<BendingAbility, List<TempBlock>> temporaries = new HashMap<>();
	private static List<TempBlock> globals = Collections.synchronizedList(new LinkedList<>());

	private Block block;
	private BlockState savedState;
	private boolean bendAllowed;
	private BendingAbility ability;
	private long started;
	private long duration;
	
	public static TempBlock makeGlobal(long duration, Block block, Material newType, boolean bendAllowed) {
		return makeGlobal(duration, block, newType, null, bendAllowed);
	}
	
	public static TempBlock makeGlobal(long duration, Block block, Material newType, BlockData newData, boolean bendAllowed) {
		return make(null, duration, block, newType, newData, bendAllowed);
	}

	public static TempBlock makeTemporary(BendingAbility ability, Block block, Material newType, boolean bendAllowed) {
		return makeTemporary(ability, block, newType, null, bendAllowed);
	}
	
	public static TempBlock makeTemporary(BendingAbility ability, Block block, Material newType, BlockData newData, boolean bendAllowed) {
		return make(ability, 0, block, newType, newData, bendAllowed);
	}

	private static TempBlock make(BendingAbility ability, long duration, Block block, Material newType, BlockData newData, boolean bendAllowed) {
		// Reuse current on if existing
		TempBlock temp;
		if (instances.containsKey(block)) {
			temp = instances.remove(block);
			if(temp.ability != null) {
				List<TempBlock> temps = temporaries.get(temp.ability);
				temps.remove(temp);
				if(temps.isEmpty()) {
					temporaries.remove(temp.ability);
				}
			}
			else {
				globals.remove(temp);
			}
		}
		else {
			temp = new TempBlock(block);
		}
		
		temp.bendAllowed = bendAllowed;
		temp.ability = ability;
		temp.started = System.currentTimeMillis();
		temp.duration = duration;

		if(block.getState() instanceof Container
			&& block.getType() != newType) {
			//Mandatory condition to avoid Containers' content to be dropped when setType() is called (Causing a duplication bug when the TempBlock is reverted)
			//Remove this when SPIGOT-3725 is resolved : hub.spigotmc.org/jira/projects/SPIGOT/issues/SPIGOT-3725
			((Container) block.getState()).getInventory().clear();
		}

		temp.block.setType(newType, false);
		if(newData != null) {
			temp.block.setBlockData(newData);
		}
		if (temp.savedState.getType() == Material.FIRE) {
			temp.savedState.setType(Material.AIR);
		}
		
		instances.put(temp.block, temp);
		if(temp.ability != null) {
			List<TempBlock> temps = temporaries.computeIfAbsent(temp.ability, k -> new LinkedList<>());
			temps.add(temp);
		}
		else {
			globals.add(temp);
		}
		
		return temp;
	}

	private TempBlock(Block block) {
		this.block = block;
		this.savedState = block.getState();
	}

	public void revertBlock() {
		revertBlock(true);
	}

	public void revertBlock(final boolean applyPhysics) {
		if(instances.containsKey(block)) {
			restoreSavedState(applyPhysics);

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
	}

	public static void revertForAbility(final BendingAbility ability) {
		revertForAbility(ability, true);
	}

	public static void revertForAbility(final BendingAbility ability, final boolean applyPhysics) {
		if (ability != null) {
			List<TempBlock> tempBlocks = temporaries.remove(ability);
			if (tempBlocks != null && !tempBlocks.isEmpty()) {
				for (TempBlock tempBlock : tempBlocks) {
					tempBlock.restoreSavedState(applyPhysics);
					instances.remove(tempBlock.block);
				}
			}
		}
	}

	private void restoreSavedState(final boolean applyPhysics){
		if(updateContainerState()) {
			savedState.update(true, applyPhysics);
		}
	}

	private boolean updateContainerState(){
		// When reverting TempBlock, this method handles containers' items to avoid duplication.
		if (!(savedState instanceof Container)
				|| !(block.getState() instanceof Container)) {
			// Normal blocks.
			return true;
		}
		// This block was a Container before and while it was a TempBlock.
		if (savedState instanceof Lootable
			&& ((Lootable) savedState).getLootTable() != null) {
			// This Container was previously an unopened loot container.

			if (block.getState() instanceof Lootable
					&& ((Lootable) block.getState()).getLootTable() == null) {
				/* This loot container has been opened while being a TempBlock.
				In this precise case, due to a NPE inside NMS code, we cannot update the previous
				contents to match the new contents before restoring the saved state.
				As the players could have taken what's inside while the container was a TempBlock,
				we choose to not revert the saved state by returning false.
				We will avoid duplication, but not reverting a TempBlock can cause bugs in the future.
				*/
				return false;
			}
			/* This loot container has not been opened while being a TempBlock.
			Thus, it still has never been opened. In this case, we do not want to mess with
			its contents, as calling setContents will apparently lead to an empty container
			on its first opening.
			 */
			return true;
		}

		// Normal container.
		// Before restoring its saved state, we will first update it with the new contents
		// that the players could have put/taken inside.
		ItemStack[] newContent = ((Container) block.getState()).getInventory().getContents();
		Inventory snapshot = ((Container) savedState).getSnapshotInventory();
		try {
			snapshot.setContents(newContent);
		} catch (IllegalArgumentException e) {
			// Will only happen if one day a TempBlock is a bigger Container than its previous savedState.
			Bending.getInstance().getLogger().warning("Cannot prevent possible duplication on Container :"+block.getLocation());
		}
		return true;
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
			List<TempBlock> copy = new LinkedList<>(temps);
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
		List<TempBlock> toRevert = new LinkedList<>(instances.values());
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
			List<TempBlock> reverts = new LinkedList<>();
			for(TempBlock temp : globals) {
				if(temp.started + temp.duration < now) {
					reverts.add(temp);
				}
			}
			reverts.forEach(t -> t.revertBlock());
		}
		
	}
}
