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
import net.avatarrealms.minecraft.bending.model.BendingType;
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
	private static Map<Player, FreezeMelt> instances = new HashMap<Player, FreezeMelt>();

	public static final int defaultrange = ConfigManager.freezeMeltRange;
	public static final int defaultradius = ConfigManager.freezeMeltRadius;
	public static final int defaultDepth = ConfigManager.freezeMeltDepth;
	private IAbility parent;
	private Player player;
	private Map<Block, Byte> frozenblocks = new HashMap<Block, Byte>();

	public FreezeMelt(Player player, IAbility parent, Block block) {
		if(isFreezable(player, block)) {
			if(instances.containsKey(player)) {
				instances.get(player).freeze(block);
			} else {
				this.parent = parent;
				this.player = player;
				
				byte data = block.getData();
				block.setType(Material.ICE);
				frozenblocks.put(block, data);
				instances.put(player, this);
			}
		}
	}
	
	public FreezeMelt(Player player, IAbility parent) {
		this.parent = parent;
		this.player = player;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.PhaseChange)) {
			return;
		}

		int range = (int) PluginTools.waterbendingNightAugment(defaultrange,
				player.getWorld());
		int radius = (int) PluginTools.waterbendingNightAugment(defaultradius,
				player.getWorld());
		if (AvatarState.isAvatarState(player)) {
			range = AvatarState.getValue(range);
		}

		Location location = EntityTools.getTargetedLocation(player, range);
		int y = (int) location.getY();
		for (Block block : BlockTools.getBlocksAroundPoint(location, radius)) {
			if (block.getLocation().getY() >= y - defaultDepth) {
				new FreezeMelt(player, parent, block);
			}
		}

		bPlayer.earnXP(BendingType.Water, this);
		bPlayer.cooldown(Abilities.PhaseChange);
	}

	private static boolean isFreezable(Player player, Block block) {	
		if (Tools.isRegionProtectedFromBuild(player, Abilities.PhaseChange,
				block.getLocation())) {
			return false;
		}
		if (block.getType() == Material.WATER
				|| block.getType() == Material.STATIONARY_WATER) {
			if (WaterManipulation.canPhysicsChange(block)
					&& !TempBlock.isTempBlock(block)) {
				return true;
			}
		}

		return false;
	}

	private void freeze(Block block) {
		byte data = block.getData();
		block.setType(Material.ICE);
		frozenblocks.put(block, data);
	}


	private boolean thaw(Block block) {
		if (frozenblocks.containsKey(block)) {
			byte data = frozenblocks.get(block);
			block.setType(Material.WATER);
			block.setData(data);
			return false;
		}
		return true;
	}

	public void handleFrozenBlocks() {
		List<Block> toRemove = new LinkedList<Block>();
		for (Block block : frozenblocks.keySet()) {
			if (canThaw(block)) {
				boolean keep = thaw(block);
				if (!keep) {
					toRemove.add(block);
				}
			}
		}
		for (Block block : toRemove) {
			frozenblocks.remove(block);
		}
	}

	public boolean canThaw(Block block) {
		if (!WaterManipulation.canPhysicsChange(block)) {
			return false;
		}
		if (frozenblocks.containsKey(block)) {
			if (EntityTools.hasAbility(getPlayer(), Abilities.PhaseChange)
					&& EntityTools.canBend(getPlayer(), Abilities.PhaseChange)) {
				double range = PluginTools.waterbendingNightAugment(
						defaultrange, getPlayer().getWorld());
				if (AvatarState.isAvatarState(getPlayer())) {
					range = AvatarState.getValue(range);
				}
				//Player just changed world, allow thaw
				if(!block.getLocation().getWorld().getUID().equals(getPlayer().getLocation().getWorld().getUID())) {
					return true;
				}
				if (block.getLocation().distance(getPlayer().getLocation()) <= range) {
					return false;
				}
			}
			if (EntityTools.getBendingAbility(getPlayer()) == Abilities.OctopusForm) {
				if (block.getLocation().distance(getPlayer().getLocation()) <= OctopusForm.radius + 2) {
					return false;
				}
			}
			return true;
			
		}
		return false;
	}

	public void thawAll() {
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
		frozenblocks.clear();
	}
	
	private boolean isBlockFrozen(Block block) {
		return frozenblocks.containsKey(block);
	}

	public static boolean isFrozen(Block block) {
		for(FreezeMelt fm : instances.values()) {
			if(fm.isBlockFrozen(block)) {
				return true;
			}
		}
		return false;
	}
	
	public static void thawThenRemove(Block block) {
		for(FreezeMelt fm : instances.values()) {
			if(fm.thaw(block)) {
				fm.remove(block);
			}
		}
	}

	private void remove(Block block) {
		this.frozenblocks.remove(block);
	}
	
	public boolean isBlockLevel(Object block, Byte level) {
		return frozenblocks.get(block) == level;
	}

	public static boolean isLevel(Block block, byte level) {
		for(FreezeMelt fm : instances.values()) {
			if(fm.isBlockLevel(block, level)) {
				return true;
			}
		}
		return false;
		
	}
	
	public static void progressAll() {
		for(FreezeMelt fm : instances.values()) {
			fm.handleFrozenBlocks();
		}
	}
	
	public static void removeAll() {
		for(FreezeMelt fm : instances.values()) {
			fm.thawAll();
		}
		instances.clear();
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

	public Player getPlayer() {
		return player;
	}

}
