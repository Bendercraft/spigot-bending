package net.avatarrealms.minecraft.bending.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Level;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.abilities.Information;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class RevertChecker implements Runnable {

	static ConcurrentHashMap<Block, Block> revertQueue = new ConcurrentHashMap<Block, Block>();
	static ConcurrentHashMap<Integer, Integer> airRevertQueue = new ConcurrentHashMap<Integer, Integer>();
	private Future<ArrayList<Chunk>> returnFuture;

	static ConcurrentHashMap<Chunk, Chunk> chunks = new ConcurrentHashMap<Chunk, Chunk>();

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

			for (Player player : server.getOnlinePlayers()) {
				Chunk chunk = player.getLocation().getChunk();
				if (!chunks.contains(chunk))
					chunks.add(chunk);
			}
			return chunks;
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
				earth.putAll(BlockTools.bendedBlocks);

				for (Block block : earth.keySet()) {
					if (revertQueue.containsKey(block)){
						continue;
					}				
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
				plugin.getLogger().log(Level.SEVERE, "Exception in revert checker", e);
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
