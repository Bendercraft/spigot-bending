package net.avatarrealms.minecraft.bending.abilities.air;

import java.util.concurrent.ConcurrentHashMap;

import net.avatarrealms.minecraft.bending.abilities.water.WaterManipulation;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

public class AirBubble {

	public static ConcurrentHashMap<Integer, AirBubble> instances = new ConcurrentHashMap<Integer, AirBubble>();

	private static double defaultAirRadius = ConfigManager.airBubbleRadius;
	private static double defaultWaterRadius = ConfigManager.waterBubbleRadius;
	// private static byte full = AirBlast.full;

	// private static final byte full = 0x0;

	private Player player;
	private double radius;
	// private ConcurrentHashMap<Block, Byte> waterorigins;
	private ConcurrentHashMap<Block, BlockState> waterorigins;

	public AirBubble(Player player) {
		this.player = player;
		waterorigins = new ConcurrentHashMap<Block, BlockState>();
		instances.put(player.getEntityId(), this);
	}

	private void pushWater() {
		if (EntityTools.isBender(player, BendingType.Air)) {
			radius = defaultAirRadius;
		} else {
			radius = defaultWaterRadius;
		}
		if (EntityTools.isBender(player, BendingType.Water)
				&& Tools.isNight(player.getWorld())) {
			radius = PluginTools.waterbendingNightAugment(defaultWaterRadius,
					player.getWorld());
		}
		if (defaultAirRadius > radius
				&& EntityTools.isBender(player, BendingType.Air))
			radius = defaultAirRadius;
		Location location = player.getLocation();

		for (Block block : waterorigins.keySet()) {
			if (block.getWorld() != location.getWorld()) {
				if (block.getType() == Material.AIR || BlockTools.isWater(block))
					waterorigins.get(block).update(true);
				waterorigins.remove(block);
			} else if (block.getLocation().distance(location) > radius) {
				if (block.getType() == Material.AIR || BlockTools.isWater(block))
					waterorigins.get(block).update(true);
				waterorigins.remove(block);
			}
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
					// if (block.getData() == full)
					waterorigins.put(block, block.getState());
					// waterorigins.put(block, block.getData());

					block.setType(Material.AIR);
				}
			}
		}
	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			removeBubble();
			return false;
		}
		if (((EntityTools.getBendingAbility(player) == Abilities.AirBubble) 
				&& EntityTools.canBend(player, Abilities.AirBubble))
				|| ((EntityTools.getBendingAbility(player) == Abilities.WaterBubble) 
						&& EntityTools.canBend(player, Abilities.WaterBubble))) {
			pushWater();
			return true;
		}
		removeBubble();
		return false;
	}

	public static void handleBubbles(Server server) {

		for (Player player : server.getOnlinePlayers()) {
			if ((EntityTools.getBendingAbility(player) == Abilities.AirBubble || EntityTools
					.getBendingAbility(player) == Abilities.WaterBubble)
					&& !instances.containsKey(player.getEntityId())) {
				new AirBubble(player);
			}
		}

		for (int ID : instances.keySet()) {
			progress(ID);
		}
	}

	private void removeBubble() {
		for (Block block : waterorigins.keySet()) {
			if (block.getType() == Material.AIR || block.isLiquid())
				waterorigins.get(block).update(true);
		}
		instances.remove(player.getEntityId());
	}

	public static boolean progress(int ID) {
		return instances.get(ID).progress();
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
		for (int id : instances.keySet()) {
			instances.get(id).removeBubble();
		}
	}

	public static String getDescription() {
		return "To use, the bender must merely have the ability selected."
				+ " All water around the user in a small bubble will vanish,"
				+ " replacing itself once the user either gets too far away or selects a different ability.";
	}

}
