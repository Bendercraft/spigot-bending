package net.avatarrealms.minecraft.bending.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.model.Information;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * TODO: Author call this class in async method of bukkit scheduler, but the first thing it does in "run()" is asking to return in sync state : strange
 * @author Koudja
 *
 */
public class RevertChecker implements Runnable {
	private static Map<Block, Block> revertQueue = new HashMap<Block, Block>();
	private static Map<Integer, Integer> airRevertQueue = new HashMap<Integer, Integer>();
	private Future<ArrayList<Chunk>> returnFuture;

	private Bending plugin;

	private static final boolean safeRevert = ConfigManager.safeRevert;

	private long time;

	public RevertChecker(Bending bending) {
		plugin = bending;
	}

	private class getOccupiedChunks implements Callable<ArrayList<Chunk>> {

		private Server server;

		public getOccupiedChunks(Server server) {
			this.server = server;
		}

		@Override
		public ArrayList<Chunk> call() throws Exception {
			ArrayList<Chunk> chunks = new ArrayList<Chunk>();
			Player[] players = server.getOnlinePlayers();

			for (Player player : players) {
				Chunk chunk = player.getLocation().getChunk();
				if (!chunks.contains(chunk))
					chunks.add(chunk);
			}

			return chunks;
		}

	}
	
	public static void progressAll() {
		List<Block> toRemoveBlock = new LinkedList<Block>();
		for (Block block : revertQueue.keySet()) {
			BlockTools.revertBlock(block);
			toRemoveBlock.add(block);
		}
		for (Block block : toRemoveBlock) {
			revertQueue.remove(block);
		}
		
		List<Integer> toRemoveInteger = new LinkedList<Integer>();
		for (int i : airRevertQueue.keySet()) {
			BlockTools.revertAirBlock(i);
			toRemoveInteger.add(i);
		}
		for (int i : toRemoveInteger) {
			airRevertQueue.remove(i);
		}
	}

	public void run() {
		time = System.currentTimeMillis();

		if (ConfigManager.reverseearthbending) {
			try {
				returnFuture = plugin
						.getServer()
						.getScheduler()
						.callSyncMethod(plugin,
								new getOccupiedChunks(plugin.getServer()));
				ArrayList<Chunk> chunks = returnFuture.get();

				Map<Block, Information> earth = new HashMap<Block, Information>();
				earth.putAll(BlockTools.movedEarth);

				for (Block block : earth.keySet()) {
					if (revertQueue.containsKey(block))
						continue;
					boolean remove = true;
					Information info = earth.get(block);
					if (time < info.getTime() + ConfigManager.revertchecktime
							|| (chunks.contains(block.getChunk()) && safeRevert)) {
						remove = false;
					}
					if (remove) {
						addToRevertQueue(block);
					}
				}

				Map<Integer, Information> air = new HashMap<Integer, Information>();
				air.putAll(BlockTools.tempAir);

				for (Integer i : air.keySet()) {
					if (airRevertQueue.containsKey(i))
						continue;
					boolean remove = true;
					Information info = air.get(i);
					Block block = info.getBlock();
					if (time < info.getTime() + ConfigManager.revertchecktime
							|| (chunks.contains(block.getChunk()) && safeRevert)) {
						remove = false;
					}
					if (remove) {
						addToAirRevertQueue(i);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				PluginTools.writeToLog(ExceptionUtils.getStackTrace(e));
			}
		}
	}

	private void addToAirRevertQueue(int i) {
		if (!airRevertQueue.containsKey(i))
			airRevertQueue.put(i, i);

	}

	void addToRevertQueue(Block block) {
		if (!revertQueue.containsKey(block))
			revertQueue.put(block, block);
	}

}
