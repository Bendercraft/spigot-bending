package net.avatarrealms.minecraft.bending.abilities.water;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.AvatarState;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.model.TempBlock;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class FreezeMelt implements IAbility {
	private static Map<Block, Byte> frozenblocks = new HashMap<Block, Byte>();

	public static final int defaultrange = ConfigManager.freezeMeltRange;
	public static final int defaultradius = ConfigManager.freezeMeltRadius;
	private IAbility parent;

	public FreezeMelt(Player player, IAbility parent) {
		this.parent = parent;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.PhaseChange))
			return;

		int range = (int) PluginTools.waterbendingNightAugment(defaultrange,
				player.getWorld());
		int radius = (int) PluginTools.waterbendingNightAugment(defaultradius,
				player.getWorld());
		if (AvatarState.isAvatarState(player)) {
			range = AvatarState.getValue(range);
			// radius = AvatarState.getValue(radius);
		}

		boolean cooldown = false;

		Location location = EntityTools.getTargetedLocation(player, range);
		for (Block block : BlockTools.getBlocksAroundPoint(location, radius)) {
			if (isFreezable(player, block)) {
				freeze(player, block);
				cooldown = true;
			}
		}

		if (cooldown)
			bPlayer.cooldown(Abilities.PhaseChange);

	}

	private static boolean isFreezable(Player player, Block block) {
		if (Tools.isRegionProtectedFromBuild(player, Abilities.PhaseChange,
				block.getLocation()))
			return false;
		if (block.getType() == Material.WATER
				|| block.getType() == Material.STATIONARY_WATER)
			if (WaterManipulation.canPhysicsChange(block)
					&& !TempBlock.isTempBlock(block))
				return true;
		return false;
	}

	static void freeze(Player player, Block block) {
		if (Tools.isRegionProtectedFromBuild(player, Abilities.PhaseChange,
				block.getLocation()))
			return;
		if (TempBlock.isTempBlock(block))
			return;
		byte data = block.getData();
		block.setType(Material.ICE);
		frozenblocks.put(block, data);
	}
	
	public static void thawThenRemove(Block block) {
		boolean keep = thaw(block);
		if(!keep) {
			frozenblocks.remove(block);
		}
	}
	
	public static void remove(Block block) {
		frozenblocks.remove(block);
	}

	private static boolean thaw(Block block) {
		if (frozenblocks.containsKey(block)) {
			byte data = frozenblocks.get(block);
			block.setType(Material.WATER);
			block.setData(data);
			return false;
		}
		return true;
	}

	public static void handleFrozenBlocks() {
		List<Block> toRemove = new LinkedList<Block>();
		for (Block block : frozenblocks.keySet()) {
			if (canThaw(block)) {
				boolean keep = thaw(block);
				if(!keep) {
					toRemove.add(block);
				}
			}
		}
		for (Block block : toRemove) {
			frozenblocks.remove(block);
		}
	}

	public static boolean canThaw(Block block) {
		if (frozenblocks.containsKey(block)) {
			for (Player player : block.getWorld().getPlayers()) {
				if (EntityTools.getBendingAbility(player) == Abilities.OctopusForm) {
					if (block.getLocation().distance(player.getLocation()) <= OctopusForm.radius + 2)
						return false;
				}
				if (EntityTools.hasAbility(player, Abilities.PhaseChange)
						&& EntityTools.canBend(player, Abilities.PhaseChange)) {
					double range = PluginTools.waterbendingNightAugment(defaultrange,
							player.getWorld());
					if (AvatarState.isAvatarState(player)) {
						range = AvatarState.getValue(range);
					}
					if (block.getLocation().distance(player.getLocation()) <= range)
						return false;
				}
			}
		}
		if (!WaterManipulation.canPhysicsChange(block))
			return false;
		return true;
	}

	private static void thawAll() {
		List<Block> toRemove = new LinkedList<Block>();
		for (Entry<Block, Byte> entry : frozenblocks.entrySet()) {
			Block block = entry.getKey();
			if (block.getType() == Material.ICE) {
				byte data = entry.getValue();
				block.setType(Material.WATER);
				block.setData(data);
				toRemove.add(block);
			}
		}
		for (Block block : toRemove) {
			frozenblocks.remove(block);
		}
	}
	
	public static boolean isFrozen(Block block) {
		return frozenblocks.containsKey(block);
	}
	
	public static boolean isLevel(Block block, byte level) {
		return frozenblocks.get(block) == level;
	}

	public static void removeAll() {
		thawAll();
	}

	public static String getDescription() {
		return "To use, simply left-click. "
				+ "Any water you are looking at within range will instantly freeze over into solid ice. "
				+ "Provided you stay within range of the ice and do not unbind FreezeMelt, "
				+ "that ice will not thaw. If, however, you do either of those the ice will instantly thaw. "
				+ "If you sneak (default: shift), anything around where you are looking at will instantly melt. "
				+ "Since this is a more favorable state for these things, they will never re-freeze unless they "
				+ "would otherwise by nature or some other bending ability. Additionally, if you tap sneak while "
				+ "targetting water with FreezeMelt, it will evaporate water around that block that is above "
				+ "sea level. ";
	}

	@Override
	public int getBaseExperience() {
		return 1;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}

}
