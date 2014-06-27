package net.avatarrealms.minecraft.bending.abilities.earth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.IAbility;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;

public class Collapse implements IAbility {
	private static final int range = ConfigManager.collapseRange;
	private static final double defaultradius = ConfigManager.collapseRadius;
	private static final int height = EarthColumn.standardheight;

	//TODO : This map is never cleared of any of its item, strange
	private Map<Block, Block> blocks = new HashMap<Block, Block>();
	private Map<Block, Integer> baseblocks = new HashMap<Block, Integer>();
	private double radius = defaultradius;
	private IAbility parent;
	private Player player;

	public Collapse(Player player, IAbility parent) {
		this.parent = parent;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.Collapse))
			return;

		this.player = player;
		Block sblock = BlockTools.getEarthSourceBlock(player, range);
		Location location;
		if (sblock == null) {
			location = EntityTools.getTargetBlock(player, range, BlockTools.getTransparentEarthbending()).getLocation();
		} else {
			location = sblock.getLocation();
		}
		for (Block block : BlockTools.getBlocksAroundPoint(location, radius)) {
			if (BlockTools.isEarthbendable(player, block)
					&& !blocks.containsKey(block)
					&& block.getY() >= location.getBlockY()) {
				getAffectedBlocks(block);
			}
		}

		if (!baseblocks.isEmpty()) {
			bPlayer.cooldown(Abilities.Collapse);
			bPlayer.earnXP(BendingType.Earth, this);
		}

		for (Block block : baseblocks.keySet()) {
			new CompactColumn(player, block.getLocation(), this);
		}
	}

	private void getAffectedBlocks(Block block) {
		Block baseblock = block;
		int tall = 0;
		List<Block> bendableblocks = new ArrayList<Block>();
		bendableblocks.add(block);
		for (int i = 1; i <= height; i++) {
			Block blocki = block.getRelative(BlockFace.DOWN, i);
			if (BlockTools.isEarthbendable(player, blocki)) {
				baseblock = blocki;
				bendableblocks.add(blocki);
				tall++;
			} else {
				break;
			}
		}
		baseblocks.put(baseblock, tall);
		for (Block blocki : bendableblocks) {
			blocks.put(blocki, baseblock);
		}

	}

	public static String getDescription() {
		return " To use, simply left-click on an earthbendable block. "
				+ "That block and the earthbendable blocks above it will be shoved "
				+ "back into the earth below them, if they can. "
				+ "This ability does have the capacity to trap something inside of it, "
				+ "although it is incredibly difficult to do so. "
				+ "Additionally, press sneak with this ability to affect an area around your targetted location - "
				+ "all earth that can be moved downwards will be moved downwards. "
				+ "This ability is especially risky or deadly in caves, depending on the "
				+ "earthbender's goal and technique.";
	}

	@Override
	public int getBaseExperience() {
		return 9;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}
