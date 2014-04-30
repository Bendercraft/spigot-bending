package model.waterbending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;

import dataAccess.ConfigManager;
import business.Tools;

public class Plantbending {

	private static final long regrowtime = ConfigManager.plantbendingRegrowTime;
	private static ConcurrentHashMap<Integer, Plantbending> instances = new ConcurrentHashMap<Integer, Plantbending>();

	private static int ID = Integer.MIN_VALUE;

	private Block block;
	private Material type;
	private byte data;
	private long time;
	private int id;

	public Plantbending(Block block) {
		if (regrowtime != 0) {
			this.block = block;
			type = block.getType();
			data = block.getData();
			time = System.currentTimeMillis() + regrowtime / 2
					+ (long) (Math.random() * (double) regrowtime) / 2;
			id = ID;
			instances.put(id, this);
			if (ID >= Integer.MAX_VALUE) {
				ID = Integer.MIN_VALUE;
			} else {
				ID++;
			}
		}
	}

	private void revert() {
		if (block.getType() == Material.AIR) {
			block.setType(type);
			block.setData(data);
		} else {
			Tools.dropItems(block, Tools.getDrops(block, type, data, null));
		}
		instances.remove(id);
	}

	public static void regrow() {
		for (int id : instances.keySet()) {
			Plantbending plantbending = instances.get(id);
			if (plantbending.time < System.currentTimeMillis()) {
				plantbending.revert();
			}
		}
	}

	public static void regrowAll() {
		for (int id : instances.keySet())
			instances.get(id).revert();
	}

	public static String getDescription() {
		return "Plantbending gives great utility to waterbenders. Provided you have Plantbending bound to any of your slots, "
				+ "it augments the rest of your abilities. Instead of being limited to water, "
				+ "snow and ice for sources of water, you can use any plant as a water source. "
				+ "So instead of focusing your ability on water, you could, for example, focus it on a "
				+ "block of leaves and it would suck the water out of the leaves for your other technique. "
				+ "Additionally, if you are close to a plant, you can click this ability to turn the plant into a "
				+ "source block of water.";
	}
}
