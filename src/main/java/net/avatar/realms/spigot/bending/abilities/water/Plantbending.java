package net.avatar.realms.spigot.bending.abilities.water;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.controller.ConfigManager;
import net.avatar.realms.spigot.bending.utils.BlockTools;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class Plantbending implements IAbility {
	private static final long regrowtime = ConfigManager.plantbendingRegrowTime;
	private static Map<Integer, Plantbending> instances = new HashMap<Integer, Plantbending>();

	private static int ID = Integer.MIN_VALUE;

	private Block block;
	private Material type;
	private byte data;
	private long time;
	private int id;
	private IAbility parent;

	public Plantbending(Block block, IAbility parent) {
		this.parent = parent;
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
	
	private void remove() {
		this.clear();
		instances.remove(id);
	}
	
	private void clear() {
		if (block.getType() == Material.AIR) {
			block.setType(type);
			block.setData(data);
		} else {
			BlockTools.dropItems(block, BlockTools.getDrops(block, type, data, null));
		}
	}

	public static void regrow() {
		List<Plantbending> toRemove = new LinkedList<Plantbending>();
		for (Plantbending plantbending : instances.values()) {
			if (plantbending.time < System.currentTimeMillis()) {
				toRemove.add(plantbending);
			}
		}
		for (Plantbending plantbending : toRemove) {
			plantbending.remove();
		}
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

	@Override
	public IAbility getParent() {
		return parent;
	}
}
