package firebending;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import tools.ConfigManager;
import tools.Tools;
import waterbending.Melt;

public class HeatMelt {

	private static final int range = ConfigManager.heatMeltRange;
	private static final int radius = ConfigManager.heatMeltRadius;

	public HeatMelt(Player player) {
		Location location = Tools.getTargetedLocation(player,
				(int) Tools.firebendingDayAugment(range, player.getWorld()));
		for (Block block : Tools.getBlocksAroundPoint(location,
				(int) Tools.firebendingDayAugment(radius, player.getWorld()))) {
			if (Tools.isMeltable(block)) {
				Melt.melt(player, block);
			} else if (isHeatable(block)) {
				heat(block);
			}
		}
	}

	private static void heat(Block block) {
		if (block.getType() == Material.OBSIDIAN) {
			block.setType(Material.LAVA);
			block.setData((byte) 0x0);
		}
	}

	private static boolean isHeatable(Block block) {
		return false;
	}

	public static String getDescription() {
		return "To use, simply left-click. "
				+ "Any meltable blocks around that target location will immediately melt.";
	}

}
