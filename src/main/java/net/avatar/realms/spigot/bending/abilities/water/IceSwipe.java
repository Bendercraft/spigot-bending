package net.avatar.realms.spigot.bending.abilities.water;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@BendingAbility(name = "Ice Swipe", element = BendingType.Water)
public class IceSwipe extends ActiveAbility {

	@ConfigurationParameter("Range")
	private static int RANGE = 25;

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 4;

	@ConfigurationParameter("Speed")
	private static double SPEED = 25;

	@ConfigurationParameter("Push-Factor")
	private static double PUSH = 1.0;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 10000;

	private Map<Block, Location> iceblocks;

	// TODO : As Kya against Zaheer
	// http://heyitsmonikashay.tumblr.com/post/91866392965/gif-request-for-imkindaprettygay-kya-vs-zaheer
	// http://37.media.tumblr.com/3154e22530fc425125e1155e7d30d3f0/tumblr_n8pysnfjYW1rkmjxwo2_250.gif

	public IceSwipe(Player player) {
		super(player, null);
		iceblocks = new HashMap<Block, Location>();
	}

	public boolean sneak() {
		switch (state) {
			case None:
				return false;
			default:
				return false;
		}
	}

	// public static void prepare(Player player) {
	// // When they click
	// if (instances.containsKey(player)) {
	// instances.get(player).launchBlock();
	// } else {
	// Block source = BlockTools.getWaterSourceBlock(player, RANGE,
	// EntityTools.canPlantbend(player));
	//
	// if (source != null &&
	// !ProtectionManager.isRegionProtectedFromBending(player,
	// Abilities.IceSwipe, source.getLocation())) {
	// new IceSwipe(player, source, null);
	// }
	// }
	// }

	public boolean progress() {
		if (!super.progress()) {
			return false;
		}

		return true;
	}

	//
	// public static void start(Player player) {
	// // When they sneak
	// if (instances.containsKey(player)) {
	// instances.get(player).started = true;
	// }
	// }

	public void manageBlocks() {
		List<Block> blocksToRemove = new LinkedList<Block>();
		for (Block block : iceblocks.keySet()) {
			if (block.getLocation().distance(player.getLocation()) > RANGE) {
				blocksToRemove.add(block);
				continue;
			}

			if (ProtectionManager.isRegionProtectedFromBending(player, Abilities.IceSwipe, block.getLocation())) {
				blocksToRemove.add(block);
				continue;
			}

			// I feel it will be very laggy
			for (LivingEntity entity : EntityTools.getLivingEntitiesAroundPoint(block.getLocation(), 1)) {
				if (entity.getUniqueId() == player.getUniqueId()) {
					continue;
				}
				blockHit(block, entity);
				blocksToRemove.add(block);
			}
		}

		for (Block block : blocksToRemove) {
			iceblocks.remove(block);
		}
	}

	public void moveWaterAround() {

	}

	public void blockHit(Block block, LivingEntity entity) {
		// Don't know if 'block' is going to be useful, may disappear

	}

	public void launchBlock() {
		Block waterblock = BlockTools.getWaterSourceBlock(player, RANGE, EntityTools.canPlantbend(player));
		if (waterblock != null && waterblock.getType() != Material.AIR) {
			Location targetloc = EntityTools.getTargetBlock(player, RANGE, BlockTools.getTransparentEarthbending()).getLocation();

			if (targetloc != null) {
				waterblock.setType(Material.ICE);
				iceblocks.put(waterblock, targetloc);
			}
		}
	}

	@Override
	public Object getIdentifier() {
		return player;
	}

	@Override
	public Abilities getAbilityType() {
		return Abilities.IceSwipe;
	}
}
