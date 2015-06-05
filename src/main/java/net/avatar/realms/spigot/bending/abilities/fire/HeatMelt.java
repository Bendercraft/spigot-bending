package net.avatar.realms.spigot.bending.abilities.fire;

import net.avatar.realms.spigot.bending.abilities.IAbility;
import net.avatar.realms.spigot.bending.abilities.water.Melt;
import net.avatar.realms.spigot.bending.controller.ConfigManager;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class HeatMelt implements IAbility {
	private static final int range = ConfigManager.heatMeltRange;
	private static final int radius = ConfigManager.heatMeltRadius;
	private IAbility parent;

	public HeatMelt(Player player, IAbility parent) {
		this.parent = parent;
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

	@Override
	public IAbility getParent() {
		return parent;
	}

}
