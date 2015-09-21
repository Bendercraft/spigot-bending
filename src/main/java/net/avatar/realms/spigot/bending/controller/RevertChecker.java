package net.avatar.realms.spigot.bending.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;

import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.deprecated.Information;
import net.avatar.realms.spigot.bending.utils.BlockTools;

public class RevertChecker implements Runnable {

	static Map<Block, Block> revertQueue = new HashMap<Block, Block>();
	static Map<Integer, Integer> airRevertQueue = new HashMap<Integer, Integer>();
	private Future<ArrayList<Chunk>> returnFuture;

	static Map<Chunk, Chunk> chunks = new HashMap<Chunk, Chunk>();

	private Bending plugin;

	private long time;

	public RevertChecker(Bending bending) {
		this.plugin = bending;
	}

	private class getOccupiedChunks implements Callable<ArrayList<Chunk>> {

		private Server server;

		public getOccupiedChunks(Server server) {
			this.server = server;
		}

		@Override
		public ArrayList<Chunk> call() throws Exception {
			ArrayList<Chunk> chunks = new ArrayList<Chunk>();

			for (Player player : this.server.getOnlinePlayers()) {
				Chunk chunk = player.getLocation().getChunk();
				if (!chunks.contains(chunk)) {
					chunks.add(chunk);
				}
			}
			return chunks;
		}
	}

	@Override
	public void run() {
		this.time = System.currentTimeMillis();

		if (Settings.REVERSE_BENDING) {
			try {
				this.returnFuture = this.plugin.getServer().getScheduler().callSyncMethod(this.plugin, new getOccupiedChunks(this.plugin.getServer()));
				ArrayList<Chunk> chunks = this.returnFuture.get();

				Map<Block, Information> earth = new HashMap<Block, Information>();
				earth.putAll(BlockTools.bendedBlocks);

				for (Block block : earth.keySet()) {
					if (revertQueue.containsKey(block)) {
						continue;
					}
					boolean remove = true;
					Information info = earth.get(block);
					if ((this.time < (info.getTime() + Settings.REVERSE_TIME)) || (chunks.contains(block.getChunk()) && Settings.SAFE_REVERSE)) {
						remove = false;
					}
					if (remove) {
						addToRevertQueue(block);
					}
				}

				Map<Integer, Information> air = new HashMap<Integer, Information>();
				air.putAll(BlockTools.tempAir);

				for (Integer i : air.keySet()) {
					if (airRevertQueue.containsKey(i)) {
						continue;
					}
					boolean remove = true;
					Information info = air.get(i);
					Block block = info.getBlock();
					if ((this.time < (info.getTime() + Settings.REVERSE_TIME)) || (chunks.contains(block.getChunk()) && Settings.SAFE_REVERSE)) {
						remove = false;
					}
					if (remove) {
						addToAirRevertQueue(i);
					}
				}
			} catch (Exception e) {
				this.plugin.getLogger().log(Level.SEVERE, "Exception in revert checker", e);
			}
		}
	}

	private void addToAirRevertQueue(int i) {
		if (!airRevertQueue.containsKey(i)) {
			airRevertQueue.put(i, i);
		}

	}

	void addToRevertQueue(Block block) {
		if (!revertQueue.containsKey(block)) {
			revertQueue.put(block, block);
		}
	}

}
