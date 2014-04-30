package earthbending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import tools.Abilities;
import tools.BendingPlayer;
import tools.ConfigManager;
import tools.Tools;

public class Collapse {

	private static final int range = ConfigManager.collapseRange;
	private static final double defaultradius = ConfigManager.collapseRadius;
	private static final int height = EarthColumn.standardheight;

	private ConcurrentHashMap<Block, Block> blocks = new ConcurrentHashMap<Block, Block>();
	private ConcurrentHashMap<Block, Integer> baseblocks = new ConcurrentHashMap<Block, Integer>();
	private double radius = defaultradius;

	private Player player;

	public Collapse(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.isOnCooldown(Abilities.Collapse))
			return;

		// if (AvatarState.isAvatarState(player))
		// radius = AvatarState.getValue(defaultradius);
		this.player = player;
		Block sblock = Tools.getEarthSourceBlock(player, range);
		Location location;
		if (sblock == null) {
			location = player.getTargetBlock(
					Tools.getTransparentEarthbending(), range).getLocation();
		} else {
			location = sblock.getLocation();
		}
		for (Block block : Tools.getBlocksAroundPoint(location, radius)) {
			if (Tools.isEarthbendable(player, block)
					&& !blocks.containsKey(block)
					&& block.getY() >= location.getBlockY()) {
				getAffectedBlocks(block);
			}
		}

		if (!baseblocks.isEmpty()) {
			bPlayer.cooldown(Abilities.Collapse);
		}

		for (Block block : baseblocks.keySet()) {
			new CompactColumn(player, block.getLocation());
		}
	}

	private void getAffectedBlocks(Block block) {
		Block baseblock = block;
		int tall = 0;
		ArrayList<Block> bendableblocks = new ArrayList<Block>();
		bendableblocks.add(block);
		for (int i = 1; i <= height; i++) {
			Block blocki = block.getRelative(BlockFace.DOWN, i);
			if (Tools.isEarthbendable(player, blocki)) {
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
}
