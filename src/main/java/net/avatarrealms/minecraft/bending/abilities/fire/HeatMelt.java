package net.avatarrealms.minecraft.bending.abilities.fire;

import net.avatarrealms.minecraft.bending.abilities.water.Melt;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class HeatMelt {
	private static final int range = ConfigManager.heatMeltRange;
	private static final int radius = ConfigManager.heatMeltRadius;

	public HeatMelt(Player player) {
		Location location = EntityTools.getTargetedLocation(player,
				(int) PluginTools.firebendingDayAugment(range, player.getWorld()));
		for (Block block : BlockTools.getBlocksAroundPoint(location,
				(int) PluginTools.firebendingDayAugment(radius, player.getWorld()))) {
			if (BlockTools.isMeltable(block)) {
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
