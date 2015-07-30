package net.avatar.realms.spigot.bending.abilities.air;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.abilities.water.WaterBubble;
import net.avatar.realms.spigot.bending.abilities.water.WaterManipulation;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;
import net.avatar.realms.spigot.bending.utils.Tools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

@BendingAbility(name="Air Bubble", element=BendingType.Air)
public class AirBubble implements IAbility {

	private static Map<Integer, AirBubble> instances = new HashMap<Integer, AirBubble>();

	@ConfigurationParameter("Radius")
	private static double DEFAULT_RADIUS = 4;

	private Player player;
	private double radius;
	private Location lastLocation;

	private Map<Block, BlockState> waterorigins;

	private IAbility parent;

	public AirBubble(Player player, IAbility parent) {
		// If already present just leave it as it is
		if (instances.containsKey(player.getEntityId())) {
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

		// Do not bother entering this loop if player location has not been
		// modified
		if (!BlockTools.locationEquals(lastLocation,location)) {
			if (EntityTools.isBender(player, BendingType.Water)) {
				radius = WaterBubble.DEFAULT_RADIUS;
				if (Tools.isNight(player.getWorld())) {
					radius = PluginTools.waterbendingNightAugment(
							WaterBubble.DEFAULT_RADIUS, player.getWorld());
				}
			} else {
				radius = DEFAULT_RADIUS;
			}

			if (DEFAULT_RADIUS > radius
					&& EntityTools.isBender(player, BendingType.Air)) {
				radius = DEFAULT_RADIUS;
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

			for (Block block : toRemove) {
				if (block.getType() == Material.AIR
						|| BlockTools.isWater(block))
					waterorigins.get(block).update(true, false);
				waterorigins.remove(block);
			}

			for (Block block : BlockTools
					.getBlocksAroundPoint(location, radius)) {
				if (waterorigins.containsKey(block))
					continue;
				if (ProtectionManager.isRegionProtectedFromBending(player,
						Abilities.AirBubble, block.getLocation()))
					continue;
				if (block.getType() == Material.STATIONARY_WATER
						|| block.getType() == Material.WATER) {
					if (WaterManipulation.canBubbleWater(block)) {
						waterorigins.put(block, block.getState());
						block.setType(Material.AIR);
					}
				}
			}
			this.lastLocation = player.getLocation();
		}
	}

	private boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			return false;
		}
		if ((EntityTools.getBendingAbility(player) == Abilities.AirBubble 
				&& EntityTools.canBend(player, Abilities.AirBubble))
				|| (EntityTools.getBendingAbility(player) == Abilities.WaterBubble) 
						&& EntityTools.canBend(player, Abilities.WaterBubble)) {
			pushWater();
			return true;
		}
		return false;
	}

	public static void progressAll() {
		List<AirBubble> toRemove = new LinkedList<AirBubble>();
		for (AirBubble bubble : instances.values()) {
			boolean keep = bubble.progress();
			if (!keep) {
				toRemove.add(bubble);
			}
		}
		for (AirBubble bubble : toRemove) {
			bubble.removeBubble();
		}
	}

	private void clearBubble() {
		for (Entry<Block, BlockState> entry : waterorigins.entrySet()) {
			if (entry.getKey().getType() == Material.AIR
					|| entry.getKey().isLiquid()) {
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

	@Override
	public IAbility getParent() {
		return parent;
	}

}
