package net.avatar.realms.spigot.bending.abilities.fire;

import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.deprecated.IAbility;
import net.avatar.realms.spigot.bending.abilities.water.Melt;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

@BendingAbility(name="Heat Melt", element=BendingType.Fire)
public class HeatMelt implements IAbility {
	
	@ConfigurationParameter("Range")
	private static final int RANGE = 15;
	
	@ConfigurationParameter("Radius")
	private static final int RADIUS = 5;
	
	private IAbility parent;

	public HeatMelt(Player player, IAbility parent) {
		this.parent = parent;
		Location location = EntityTools.getTargetedLocation(player,
				(int) PluginTools.firebendingDayAugment(RANGE, player.getWorld()));
		for (Block block : BlockTools.getBlocksAroundPoint(location,
				(int) PluginTools.firebendingDayAugment(RADIUS, player.getWorld()))) {
			if (BlockTools.isMeltable(block)) {
				Melt.melt(player, block);
			} else if (isHeatable(block)) {
				heat(block);
			}
		}
	}

	@SuppressWarnings("deprecation")
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
