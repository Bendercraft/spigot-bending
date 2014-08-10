package net.avatarrealms.minecraft.bending.abilities.air;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.avatarrealms.minecraft.bending.abilities.water.WaterManipulation;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

public class AirBubble implements IAbility {

	private static Map<Integer, AirBubble> instances = new HashMap<Integer, AirBubble>();

	private static double defaultAirRadius = ConfigManager.airBubbleRadius;
	private static double defaultWaterRadius = ConfigManager.waterBubbleRadius;

	private Player player;
	private double radius;
	private Location lastLocation;

	private Map<Block, BlockState> waterorigins;

	private IAbility parent;

	public AirBubble(Player player, IAbility parent) {
		//If already present, cancel previous bubble
		if(instances.containsKey(player.getEntityId())) {
			AirBubble bubble = instances.get(player.getEntityId());
			bubble.removeBubble();
			return;
		}
		
		this.parent = parent;
		this.player = player;
		this.lastLocation = player.getLocation();
		waterorigins = new HashMap<Block, BlockState>();
		instances.put(player.getEntityId(), this);
	}

	private void pushWater() {
		Location location = player.getLocation();
		
		//Do not bother entering this loop if player location has not been modified
		if(!this.lastLocation.equals(location)) {
			if (EntityTools.isBender(player, BendingType.Water)) {
				radius = defaultWaterRadius;
				if (Tools.isNight(player.getWorld())) {
					radius = PluginTools.waterbendingNightAugment(defaultWaterRadius,
							player.getWorld());
				}
			}
			else {
				radius = defaultAirRadius;
			}
	
			if (defaultAirRadius > radius
					&& EntityTools.isBender(player, BendingType.Air)) {
				radius = defaultAirRadius;
				// In case he has both element
			}
			
			List<Block> toRemove = new LinkedList<Block>();
			for (Entry<Block, BlockState> entry : waterorigins.entrySet()) {
				if (entry.getKey().getWorld() != location.getWorld()) {
					toRemove.add(entry.getKey());
				} else if (entry.getKey().getLocation().distance(location) > radius) {
					toRemove.add(entry.getKey());
				}
			}
			
			for(Block block : toRemove) {
				if (block.getType() == Material.AIR || BlockTools.isWater(block))
					waterorigins.get(block).update(true);
				waterorigins.remove(block);
			}
	
			for (Block block : BlockTools.getBlocksAroundPoint(location, radius)) {
				if (waterorigins.containsKey(block))
					continue;
				if (Tools.isRegionProtectedFromBuild(player, Abilities.AirBubble,
						block.getLocation()))
					continue;
				if (block.getType() == Material.STATIONARY_WATER
						|| block.getType() == Material.WATER) {
					if (WaterManipulation.canBubbleWater(block)) {
						waterorigins.put(block, block.getState());
						block.setType(Material.AIR);
					}
				}
			}
		}
	}

	private boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			return false;
		}
		if (((EntityTools.getBendingAbility(player) == Abilities.AirBubble) 
				&& EntityTools.canBend(player, Abilities.AirBubble))
				|| ((EntityTools.getBendingAbility(player) == Abilities.WaterBubble) 
						&& EntityTools.canBend(player, Abilities.WaterBubble))) {
			pushWater();
			return true;
		}
		return false;
	}

	public static void progressAll() {
		List<AirBubble> toRemove = new LinkedList<AirBubble>();
		for (AirBubble bubble : instances.values()) {
			boolean keep = bubble.progress();
			if(!keep) {
				toRemove.add(bubble);
			}
		}
		for(AirBubble bubble : toRemove) {
			bubble.removeBubble();
		}
	}
	
	private void clearBubble() {
		for (Entry<Block, BlockState> entry : waterorigins.entrySet()) {
			if (entry.getKey().getType() == Material.AIR || entry.getKey().isLiquid()){
				entry.getValue().update(true);
			}			
		}
	}

	private void removeBubble() {
		this.clearBubble();
		instances.remove(player.getEntityId());
	}

	public boolean blockInBubble(Block block) {
		if (block.getWorld() != player.getWorld()) {
			return false;
		}
		if (block.getLocation().distance(player.getLocation()) <= radius) {
			return true;
		}
		return false;
	}

	public static boolean canFlowTo(Block block) {
		for (int ID : instances.keySet()) {
			if (instances.get(ID).blockInBubble(block)) {
				return false;
			}
		}
		return true;
	}

	public static void removeAll() {
		for (AirBubble bubble : instances.values()) {
			bubble.clearBubble();
		}
		instances.clear();
	}

	public static String getDescription() {
		return "To use, the bender must merely have the ability selected."
				+ " All water around the user in a small bubble will vanish,"
				+ " replacing itself once the user either gets too far away or selects a different ability.";
	}

	@Override
	public int getBaseExperience() {
		return 0;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}
